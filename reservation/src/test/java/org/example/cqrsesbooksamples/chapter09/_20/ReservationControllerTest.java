package org.example.cqrsesbooksamples.chapter09._20;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestReservationCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationControllerTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"participantId\":\"\",\"slotId\":\"s-1\"}",
        "{\"participantId\":\" \",\"slotId\":\"s-1\"}",
        "{\"slotId\":\"s-1\"}",
        "{\"participantId\":\"p-1\",\"slotId\":\"\"}",
        "{\"participantId\":\"p-1\",\"slotId\":\" \"}",
        "{\"participantId\":\"p-1\"}"
    })
    void 必須入力の欠落はキューへ積む前に拒否する(String body) throws Exception {
        var queue = mock(ReservationRequestQueue.class);
        var controller = new ReservationController(queue, mock(ReservationStatusQueryService.class));
        MockMvcBuilders.standaloneSetup(controller).build()
            .perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(queue);
    }

    @Test
    void 受付済みを返して同じIDの予約コマンドをキューへ積む() throws Exception {
        var requestQueue = mock(ReservationRequestQueue.class);
        var statusQueryService = mock(ReservationStatusQueryService.class);
        var controller = new ReservationController(requestQueue, statusQueryService);
        var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        var result = mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"participantId":"p-1","slotId":"s-1"}
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.reservationId").isNotEmpty())
            .andReturn();

        var reservationId = new ObjectMapper()
            .readTree(result.getResponse().getContentAsString())
            .get("reservationId")
            .asText();
        var captor = ArgumentCaptor.forClass(RequestReservationCommand.class);
        verify(requestQueue).enqueue(captor.capture());
        assertThat(reservationId).isNotBlank();
        assertThat(captor.getValue()).isEqualTo(
            new RequestReservationCommand(reservationId, "p-1", "s-1"));
    }

    @Test
    void 受付IDで予約結果を追跡できる() throws Exception {
        var requestQueue = mock(ReservationRequestQueue.class);
        var statusQueryService = mock(ReservationStatusQueryService.class);
        when(statusQueryService.findById("r-1")).thenReturn(java.util.Optional.of(
            new ReservationStatusResponse("r-1", "s-1", "p-1", "SLOT_SECURED")));
        var controller = new ReservationController(requestQueue, statusQueryService);
        var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/reservations/r-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservationId").value("r-1"))
            .andExpect(jsonPath("$.status").value("SLOT_SECURED"));
    }
}
