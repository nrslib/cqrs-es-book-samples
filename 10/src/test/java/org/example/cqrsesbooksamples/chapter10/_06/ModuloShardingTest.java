// 検証補助: 10.3
package org.example.cqrsesbooksamples.chapter10._06;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 剰余方式の振り分けと、台数変更時の再配置の広がりを検証する。
 */
class ModuloShardingTest {

    @Test
    void 剰余方式はハッシュ値をサーバ数で割った余りで振り分ける() {
        assertThat(10 % 4).isEqualTo(2); // データA → サーバ2
        assertThat(17 % 4).isEqualTo(1); // データB → サーバ1
        assertThat(20 % 4).isEqualTo(0); // データC → サーバ0
    }

    // 削除済み（旧 10.7）
    @Test
    void サーバが増えるとほとんどのデータの振り分け先が変わる() {
        // 4台から5台に増やすと、データAとデータBは振り分け先が変わる
        assertThat(10 % 5).isEqualTo(0).isNotEqualTo(10 % 4); // データA: 変更
        assertThat(17 % 5).isEqualTo(2).isNotEqualTo(17 % 4); // データB: 変更
        assertThat(20 % 5).isEqualTo(0).isEqualTo(20 % 4);    // データC: 変更なし

        // 多数のキーで見ると、大半が移動してしまう
        long moved = 0;
        for (int hash = 0; hash < 1000; hash++) {
            if (hash % 4 != hash % 5) {
                moved++;
            }
        }
        assertThat(moved).isGreaterThan(500);
    }
}
