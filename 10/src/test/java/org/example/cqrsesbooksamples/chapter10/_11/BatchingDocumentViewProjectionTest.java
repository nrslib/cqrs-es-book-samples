package org.example.cqrsesbooksamples.chapter10._11;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.ArrayList;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter10_11;DB_CLOSE_DELAY=-1",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector="
                + "org.example.cqrsesbooksamples.chapter10._11.UpdateCountingStatementInspector"
})
class BatchingDocumentViewProjectionTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private DocumentViewRepository repository;

    @Test
    void 同じ行への増減がバッチ単位の少数のUPDATEに畳まれる() {
        eventGateway.publish(new DocumentCreatedEvent("doc-1"));

        UpdateCountingStatementInspector.UPDATE_COUNT.set(0);
        var events = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            events.add(new HelpfulMarkedEvent("doc-1", "user-" + i));
        }
        eventGateway.publish(events.toArray());

        await().atMost(Duration.ofSeconds(30)).ignoreExceptions().untilAsserted(() ->
            assertThat(repository.findById("doc-1").orElseThrow().getHelpfulCount()).isEqualTo(50));

        // イベント50件がバッチ単位に畳まれ、UPDATEはイベント件数よりはるかに少ない
        assertThat(UpdateCountingStatementInspector.UPDATE_COUNT.get()).isLessThanOrEqualTo(5);
    }

    @Test
    void フラッシュが失敗したバッチはトークンごと巻き戻り再試行で取りこぼさない() {
        eventGateway.publish(new DocumentCreatedEvent("doc-2"));

        // 次のフラッシュ（document_viewsへのUPDATE）を1回だけ失敗させる
        UpdateCountingStatementInspector.FAIL_NEXT_UPDATE.set(true);

        var events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            events.add(new HelpfulMarkedEvent("doc-2", "user-" + i));
        }
        eventGateway.publish(events.toArray());

        // 1回目のフラッシュ失敗でバッチ全体が巻き戻り、再試行後に全件が反映される
        await().atMost(Duration.ofSeconds(30)).ignoreExceptions().untilAsserted(() ->
            assertThat(repository.findById("doc-2").orElseThrow().getHelpfulCount()).isEqualTo(10));
    }
}
