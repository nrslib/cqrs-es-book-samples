// 検証補助: 9.17
package org.example.cqrsesbooksamples.chapter09._20;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter09._01.ReservationApplication;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.CreateReservationSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.projection.ReservationView;
import org.example.cqrsesbooksamples.chapter09._01.projection.ReservationViewRepository;
import org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailability;
import org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailabilityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = ReservationApplication.class, properties =
    "spring.datasource.url=jdbc:h2:mem:reservation_http;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ReservationHttpIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired CommandGateway commandGateway;
    @Autowired ReservationViewRepository views;
    @Autowired SlotAvailabilityRepository slots;

    @Test
    void 通常アプリのHTTPで枠作成から申込み承認取消まで辿れる() throws Exception {
        var slotResult = mvc.perform(post("/reservation-slots")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"slotDate":"%s","startTime":"10:00","endTime":"11:00","capacity":1}
                    """.formatted(LocalDate.now().plusDays(1))))
            .andExpect(status().isCreated()).andReturn();
        var slotId = mapper.readTree(slotResult.getResponse().getContentAsString()).get("slotId").asText();
        var reservationId = request(slotId, "p-1");
        awaitStatus(reservationId, "SLOT_SECURED");
        var fullReservationId = request(slotId, "p-2");
        awaitStatus(fullReservationId, "FAILED");

        mvc.perform(post("/reservations/{id}/approve", reservationId)
                .contentType(MediaType.APPLICATION_JSON).content("{\"approvedBy\":\"admin-1\"}"))
            .andExpect(status().isNoContent());
        awaitStatus(reservationId, "CONFIRMED");
        mvc.perform(post("/reservations/{id}/cancel", reservationId)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"参加者都合\"}"))
            .andExpect(status().isNoContent());
        awaitStatus(reservationId, "CANCELLED");
        await().untilAsserted(() -> assertThat(slots.findById(slotId).map(SlotAvailability::getRemaining))
            .contains(1));
        awaitStatus(request(slotId, "p-3"), "SLOT_SECURED");
        mvc.perform(post("/reservations/{id}/cancel", reservationId)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"再取消\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void 枠の不正入力と存在しない予約の操作を拒否する() throws Exception {
        mvc.perform(post("/reservation-slots").contentType(MediaType.APPLICATION_JSON)
                .content("{\"capacity\":1}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/reservation-slots").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"slotDate":"%s","startTime":"10:00","endTime":"11:00","capacity":0}
                    """.formatted(LocalDate.now().plusDays(1))))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/reservations/{id}/approve", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{\"approvedBy\":\"admin-1\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void ドメインで恒久拒否される入力は受付せず正常な後続を処理できる() throws Exception {
        var slotId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new CreateReservationSlotCommand(slotId, LocalDate.now().plusDays(1),
            LocalTime.of(10, 0), LocalTime.of(11, 0), 1));
        assertThatThrownBy(() -> commandGateway.sendAndWait(
            new RequestReservationCommand(UUID.randomUUID().toString(), "", slotId)))
            .isInstanceOf(IllegalArgumentException.class);

        var queue = new ReservationRequestQueue(commandGateway);
        var controller = new ReservationController(queue, new ReservationStatusQueryService(views));
        var ingress = MockMvcBuilders.standaloneSetup(controller).build();
        var invalid = ingress.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(java.util.Map.of("participantId", "", "slotId", slotId))))
            .andReturn().getResponse();
        var valid = ingress.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(java.util.Map.of("participantId", "p-2", "slotId", slotId))))
            .andExpect(status().isAccepted()).andReturn().getResponse();

        queue.dispatchNext();
        queue.dispatchNext();

        assertThat(invalid.getStatus()).isEqualTo(400);
        var reservationId = mapper.readTree(valid.getContentAsString()).get("reservationId").asText();
        await().untilAsserted(() -> assertThat(views.findById(reservationId).map(ReservationView::getStatus))
            .contains("SLOT_SECURED"));
    }

    private String request(String slotId, String participantId) throws Exception {
        var response = mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(java.util.Map.of("participantId", participantId, "slotId", slotId))))
            .andExpect(status().isAccepted()).andReturn().getResponse();
        return mapper.readTree(response.getContentAsString()).get("reservationId").asText();
    }

    private void awaitStatus(String reservationId, String expected) {
        await().untilAsserted(() -> mvc.perform(get("/reservations/{id}", reservationId))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(expected)));
    }
}
