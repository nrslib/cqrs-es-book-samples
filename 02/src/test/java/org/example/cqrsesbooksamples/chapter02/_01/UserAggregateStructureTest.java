// 検証補助: 2.1
package org.example.cqrsesbooksamples.chapter02._1;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.modelling.command.inspection.AnnotatedAggregateMetaModelFactory;
import org.junit.jupiter.api.Test;

/**
 * 最小構成のAggregate（リスト2.1）の検証。
 * Axonのメタモデル検査に通ること、デフォルトコンストラクタで
 * 再構築用のインスタンスを生成し識別子フィールドが認識されることを確かめる。
 */
class UserAggregateStructureTest {

    @Test
    void Axonのアグリゲートとして識別子付きで認識される() {
        var model = AnnotatedAggregateMetaModelFactory.inspectAggregate(UserAggregate.class);

        // イベントソーシングの再構築で使われるデフォルトコンストラクタ
        var aggregate = new UserAggregate();

        assertThat(model.type()).isEqualTo("UserAggregate");
        // @AggregateIdentifier のフィールドが識別子として解決される（未設定なのでnull）
        assertThat(model.getIdentifier(aggregate)).isNull();
    }
}
