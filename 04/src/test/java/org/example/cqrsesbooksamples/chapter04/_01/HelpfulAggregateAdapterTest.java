package org.example.cqrsesbooksamples.chapter04._01;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HelpfulAggregateAdapterTest {
    private AggregateTestFixture<HelpfulAggregateAdapter> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(HelpfulAggregateAdapter.class);
    }

    @Test
    void 初回の評価で集約が作られHelpfulMarkedEventが発行される() {
        fixture.givenNoPriorActivity()
            .when(new MarkAsHelpfulCommand("doc123", "user456"))
            .expectEvents(new HelpfulMarkedEvent("doc123", "user456"));
    }

    @Test
    void 既存の評価に対するマークはトグルとして解除イベントになる() {
        fixture.givenCommands(new MarkAsHelpfulCommand("doc123", "user456"))
            .when(new MarkAsHelpfulCommand("doc123", "user456"))
            .expectEvents(new HelpfulUnmarkedEvent("doc123", "user456"));
    }

    @Test
    void 評価済みの解除でHelpfulUnmarkedEventが発行される() {
        fixture.givenCommands(new MarkAsHelpfulCommand("doc123", "user456"))
            .when(new UnmarkAsHelpfulCommand("doc123", "user456"))
            .expectEvents(new HelpfulUnmarkedEvent("doc123", "user456"));
    }

    @Test
    void すでに解除済みの評価をさらに解除すると例外になる() {
        fixture.givenCommands(
                new MarkAsHelpfulCommand("doc123", "user456"),
                new UnmarkAsHelpfulCommand("doc123", "user456"))
            .when(new UnmarkAsHelpfulCommand("doc123", "user456"))
            .expectException(IllegalStateException.class);
    }
}
