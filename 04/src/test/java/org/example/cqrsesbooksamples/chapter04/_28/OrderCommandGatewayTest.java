// 検証補助: 4.24
package org.example.cqrsesbooksamples.chapter04._28;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter04._20.GetInventoryQuery;
import org.example.cqrsesbooksamples.chapter04._20.InventoryQueryResult;
import org.example.cqrsesbooksamples.chapter04._27.CreateOrderRequest;
import org.example.cqrsesbooksamples.chapter04._29.OrderAggregateAdapter;
import org.example.cqrsesbooksamples.chapter04._29.OrderCreatedEvent;
import org.example.cqrsesbooksamples.chapter04._29.OrderLine;
import org.junit.jupiter.api.Test;


class OrderCommandGatewayTest {
    @Test
    void 実Gatewayを通じて作成した注文IDと保存イベントを取得できる() {
        var queryGateway = mock(QueryGateway.class);
        when(queryGateway.query(any(GetInventoryQuery.class), eq(InventoryQueryResult.class)))
            .thenReturn(CompletableFuture.completedFuture(new InventoryQueryResult(10)));
        var configuration = DefaultConfigurer.defaultConfiguration(false)
            .configureEmbeddedEventStore(c -> new InMemoryEventStorageEngine())
            .configureAggregate(OrderAggregateAdapter.class)
            .registerCommandHandler(c -> new OrderCommandHandler(c.commandGateway(), queryGateway))
            .start();
        var controller = new OrderController(configuration.commandGateway());
        var lines = List.of(new OrderLine("product-1", 3));

        String orderId = controller.createOrder(new CreateOrderRequest(lines)).join();

        assertThat(orderId).isNotBlank();
        assertThat(configuration.eventStore().readEvents(orderId).asStream()
            .<Object>map(message -> message.getPayload()).toList())
            .containsExactly(new OrderCreatedEvent(orderId, lines));
    }

    @Test
    void 注文集約が拒否した要求は外側のGatewayでも失敗する() {
        var queryGateway = mock(QueryGateway.class);
        var configuration = DefaultConfigurer.defaultConfiguration(false)
            .configureEmbeddedEventStore(c -> new InMemoryEventStorageEngine())
            .configureAggregate(OrderAggregateAdapter.class)
            .registerCommandHandler(c -> new OrderCommandHandler(c.commandGateway(), queryGateway))
            .start();
        var controller = new OrderController(configuration.commandGateway());

        assertThatThrownBy(() -> controller.createOrder(new CreateOrderRequest(List.of())).join())
            .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }
}
