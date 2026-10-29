package org.example.cqrsesbooksamples.chapter04._11;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.cqrsesbooksamples.chapter04._01.HelpfulId;
import org.junit.jupiter.api.Test;

class HelpfulEitherTest {
    @Test
    void 解除済みの評価はLeftにエラーが入る() {
        var helpful = new Helpful(new HelpfulId("doc123", "user456"), false);

        var result = helpful.unmark();
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(AlreadyUnmarkedError.class);
    }

    @Test
    void 評価済みならRightにイベントが入る() {
        var helpful = new Helpful(new HelpfulId("doc123", "user456"), true);

        var result = helpful.unmark();
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().documentId()).isEqualTo("doc123");
    }
}
