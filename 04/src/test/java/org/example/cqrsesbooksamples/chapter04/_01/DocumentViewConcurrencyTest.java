package org.example.cqrsesbooksamples.chapter04._01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter04_01_conc;DB_CLOSE_DELAY=-1")
class DocumentViewConcurrencyTest {
    @Autowired
    private DocumentViewProjection projection;

    @Autowired
    private DocumentViewRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void 複数スレッドから並行して加算してもカウントを取りこぼさない() throws Exception {
        var tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(s ->
            projection.on(new DocumentCreatedEvent("doc-conc", "title", "content")));

        int threads = 4;
        int perThread = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < threads * perThread; i++) {
            String userId = "user-" + i;
            tasks.add(() -> {
                tx.executeWithoutResult(s ->
                    projection.on(new HelpfulMarkedEvent("doc-conc", userId)));
                return null;
            });
        }
        pool.invokeAll(tasks);
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        var view = repository.findById("doc-conc").orElseThrow();
        assertThat(view.getHelpfulCount()).isEqualTo(threads * perThread);
    }

    @Test
    void 行がまだ無ければ例外になり再試行に乗る() {
        var tx = new TransactionTemplate(transactionManager);
        assertThatThrownBy(() -> tx.executeWithoutResult(s ->
                projection.on(new HelpfulMarkedEvent("doc-missing", "user-1"))))
            .isInstanceOf(IllegalStateException.class);
    }
}
