package org.example.cqrsesbooksamples.chapter04._09;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.example.cqrsesbooksamples.chapter04._01.HelpfulId;
import org.junit.jupiter.api.Test;

class HelpfulDomainExceptionTest {
    @Test
    void 解除済みの評価を解除するとドメイン固有例外が投げられる() {
        var helpful = new Helpful(new HelpfulId("doc123", "user456"), false);

        assertThatThrownBy(helpful::unmark)
            .isInstanceOf(HelpfulDomainException.AlreadyUnmarkedException.class)
            .hasMessageContaining("doc123_user456");
    }

    @Test
    void 評価済みなら解除イベントが返る() {
        var helpful = new Helpful(new HelpfulId("doc123", "user456"), true);

        var event = helpful.unmark();
        assertThat(event.documentId()).isEqualTo("doc123");
        assertThat(event.userId()).isEqualTo("user456");
    }
}
