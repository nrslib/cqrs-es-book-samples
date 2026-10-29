// 検証補助: 4.23
package org.example.cqrsesbooksamples.chapter04._27;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter04._20.GetInventoryQuery;
import org.example.cqrsesbooksamples.chapter04._20.InventoryQueryResult;
import org.example.cqrsesbooksamples.chapter04._29.CreateOrderCommand;
import org.example.cqrsesbooksamples.chapter04._29.OrderLine;
import org.junit.jupiter.api.Test;

/**
 * コントローラで在庫確認してからコマンドを送る実装（リスト4.23）の検証。
 * 在庫が足りれば注文コマンドが送信され、不足していれば例外になり
 * コマンドが送られないことを確かめる。
 */
class OrderControllerTest {

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final QueryGateway queryGateway = mock(QueryGateway.class);
    private final OrderController controller = new OrderController(commandGateway, queryGateway);

    @Test
    void 在庫が足りていれば注文コマンドを送信する() {
        when(queryGateway.query(eq(new GetInventoryQuery("product-1")), eq(InventoryQueryResult.class)))
                .thenReturn(CompletableFuture.completedFuture(new InventoryQueryResult(10)));
        when(commandGateway.send(any(CreateOrderCommand.class)))
                .thenReturn(CompletableFuture.completedFuture("order-1"));

        var orderLines = List.of(new OrderLine("product-1", 3));
        var result = controller.createOrder(new CreateOrderRequest(orderLines)).join();

        assertThat(result).isEqualTo("order-1");
        verify(commandGateway).send(new CreateOrderCommand(orderLines));
    }

    @Test
    void 在庫が不足していれば例外になりコマンドは送られない() {
        when(queryGateway.query(eq(new GetInventoryQuery("product-1")), eq(InventoryQueryResult.class)))
                .thenReturn(CompletableFuture.completedFuture(new InventoryQueryResult(2)));

        var request = new CreateOrderRequest(List.of(new OrderLine("product-1", 3)));

        assertThatThrownBy(() -> controller.createOrder(request))
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(InsufficientInventoryException.class);
        verify(commandGateway, never()).send(any(CreateOrderCommand.class));
    }
}
