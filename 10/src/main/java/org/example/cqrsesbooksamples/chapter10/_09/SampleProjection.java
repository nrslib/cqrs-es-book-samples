package org.example.cqrsesbooksamples.chapter10._09;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("sample-projection")
public class SampleProjection {
    @EventHandler
    public void on(SomethingHappenedEvent event) {
        // セグメント運用のデモ用ハンドラ
    }
}
