package org.example.cqrsesbooksamples.chapter03._17;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Chapter03_17TestApplication {

    record UserRegisteredEvent(String userId) {}

    /** BYTEA構成のイベントストアを経由してイベントを受け取るテスト用ハンドラ */
    @Component
    @ProcessingGroup("user-projection")
    static class UserProjectionStub {
        static final List<String> received = new CopyOnWriteArrayList<>();

        @EventHandler
        public void on(UserRegisteredEvent event) {
            received.add(event.userId());
        }
    }
}
