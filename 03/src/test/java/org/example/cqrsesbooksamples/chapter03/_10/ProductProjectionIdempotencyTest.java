// 検証補助: 3.6
package org.example.cqrsesbooksamples.chapter03._10;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * べき等なプロジェクション（リスト3.6）の検証。
 * 同じイベントが重複して届いても、2回目は保存が行われないことを確かめる。
 */
class ProductProjectionIdempotencyTest {

    @Test
    void 同じイベントの再配信では追加の保存が行われない() throws Exception {
        var repository = mock(ProductRepository.class);
        var projection = new ProductProjection(repository);
        var handlerAdapter = new AnnotationEventHandlerAdapter(projection);
        var event = GenericEventMessage.asEventMessage(new ProductAddedEvent("product-1"));

        // 初回: 存在しないので保存される
        when(repository.existsById("product-1")).thenReturn(false);
        handlerAdapter.handle(event);
        verify(repository, times(1)).save(ArgumentMatchers.any(ProductReadModel.class));

        // 再配信: すでに存在するので保存されない
        when(repository.existsById("product-1")).thenReturn(true);
        handlerAdapter.handle(event);
        verify(repository, times(1)).save(ArgumentMatchers.any(ProductReadModel.class));
    }
}
