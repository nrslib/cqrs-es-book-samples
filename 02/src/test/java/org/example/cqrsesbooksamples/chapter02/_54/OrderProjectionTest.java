package org.example.cqrsesbooksamples.chapter02._54;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrderProjectionTest {
    // 削除済み（旧 2.59）
    @Test
    void 注文イベントからリードモデルが保存される() {
        var repository = mock(OrderReadModelRepository.class);
        var projection = new OrderProjection(repository);

        projection.on(new OrderPlacedEvent("order-123", 1000));

        var captor = ArgumentCaptor.forClass(OrderReadModel.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo("order-123");
        assertThat(captor.getValue().getAmount()).isEqualTo(1000);
    }
}
