package org.example.cqrsesbooksamples.chapter09._22;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubSlotRouterTest {

    @Test
    void 予約IDのハッシュで同じサブ枠を選ぶ() {
        var router = new SubSlotRouter();

        assertThat(router.selectSubSlot("s-1", "r-1")).isEqualTo("s-1-sub-2");
        assertThat(router.selectSubSlot("s-1", "r-1")).isEqualTo("s-1-sub-2");
    }
}
