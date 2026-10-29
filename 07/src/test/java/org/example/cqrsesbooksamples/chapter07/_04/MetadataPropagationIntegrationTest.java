// 検証補助: 7.3
package org.example.cqrsesbooksamples.chapter07._04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cqrsesbooksamples.chapter07._05.PaymentsController;
import org.example.cqrsesbooksamples.chapter07._09.PaymentProcessTrackerRepository;
import org.example.cqrsesbooksamples.chapter07.support.AccountData;
import org.example.cqrsesbooksamples.chapter07.support.AccountQueryHandler;
import org.example.cqrsesbooksamples.chapter07.support.NotFoundException;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.PaymentsPostRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        classes = Chapter07_04TestApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:chapter07_04;DB_CLOSE_DELAY=-1")
class MetadataPropagationIntegrationTest {
    @Autowired
    private PaymentsController controller;

    @Autowired
    private AccountQueryHandler accountQueryHandler;

    @Autowired
    private PaymentProcessTrackerRepository trackerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void コントローラが付与したprocessIdがイベントに伝搬されて収集される() throws Exception {
        var accountId = UUID.randomUUID();
        accountQueryHandler.register(new AccountData(accountId, "Taro"));

        var response = controller.post(
                new PaymentsPostRequest(accountId, new BigDecimal("5000"), PaymentMethod.CREDIT));

        // コマンドのメタデータに付与した processId が、イベントへ伝搬されて追跡テーブルに集まる
        await().untilAsserted(() -> {
            var trackers = trackerRepository.findAll();
            assertThat(trackers).hasSize(1);
            var tracker = trackers.get(0);
            assertThat(tracker.getProcessId()).isEqualTo(response.paymentProcessId());
            assertThat(tracker.getCurrentStatus()).isEqualTo("STARTED");
            assertThat(tracker.getEvents()).hasSize(1);
        });

        // 保存されたイベントのメタデータには traceId と correlationId が乗る
        byte[] metaDataBytes = jdbcTemplate.queryForObject(
            "SELECT meta_data FROM domain_event_entry ORDER BY global_index DESC LIMIT 1",
            byte[].class);
        var metaData = new ObjectMapper().readTree(metaDataBytes);
        assertThat(metaData.path("processId").asText()).isEqualTo(response.paymentProcessId());
        var traceId = metaData.path("traceId").asText();
        var correlationId = metaData.path("correlationId").asText();
        assertThat(UUID.fromString(traceId).toString()).isEqualTo(traceId);
        assertThat(UUID.fromString(correlationId).toString()).isEqualTo(correlationId);
    }

    @Test
    void 存在しないアカウントの支払いは拒否される() {
        var request = new PaymentsPostRequest(
                UUID.randomUUID(), new BigDecimal("5000"), PaymentMethod.CREDIT);

        assertThatThrownBy(() -> controller.post(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Account not found");
    }
}
