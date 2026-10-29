// 検証補助: 4.24
package org.example.cqrsesbooksamples.chapter04._28;

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
import org.example.cqrsesbooksamples.chapter04._27.CreateOrderRequest;
import org.example.cqrsesbooksamples.chapter04._27.InsufficientInventoryException;
import org.example.cqrsesbooksamples.chapter04._29.CreateOrderCommand;
import org.example.cqrsesbooksamples.chapter04._29.OrderLine;
import org.junit.jupiter.api.Test;

/**
 * 在庫確認をコマンドハンドラへ移した実装（リスト4.24）の検証。
 * コントローラは要求コマンドを送るだけになり、在庫確認と注文コマンドの
 * 発行はハンドラ側で行われることを確かめる。
 */
class OrderCommandHandlerTest {

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final QueryGateway queryGateway = mock(QueryGateway.class);

    @Test
    void コントローラは要求コマンドを送信するだけになる() {
        when(commandGateway.send(any(CreateOrderRequestCommand.class)))
                .thenReturn(CompletableFuture.completedFuture("order-1"));
        var controller = new OrderController(commandGateway);
        var orderLines = List.of(new OrderLine("product-1", 3));

        var result = controller.createOrder(new CreateOrderRequest(orderLines)).join();

        assertThat(result).isEqualTo("order-1");
        verify(commandGateway).send(new CreateOrderRequestCommand(orderLines));
    }

    @Test
    void ハンドラが在庫を確認してから注文コマンドを発行する() {
        when(queryGateway.query(eq(new GetInventoryQuery("product-1")), eq(InventoryQueryResult.class)))
                .thenReturn(CompletableFuture.completedFuture(new InventoryQueryResult(10)));
        when(commandGateway.sendAndWait(any(CreateOrderCommand.class)))
                .thenReturn("order-1");
        var handler = new OrderCommandHandler(commandGateway, queryGateway);
        var orderLines = List.of(new OrderLine("product-1", 3));

        var result = handler.handle(new CreateOrderRequestCommand(orderLines));

        assertThat(result).isEqualTo("order-1");
        verify(commandGateway).sendAndWait(new CreateOrderCommand(orderLines));
    }

    @Test
    void 在庫が不足していればハンドラで例外になり注文コマンドは発行されない() {
        when(queryGateway.query(eq(new GetInventoryQuery("product-1")), eq(InventoryQueryResult.class)))
                .thenReturn(CompletableFuture.completedFuture(new InventoryQueryResult(2)));
        var handler = new OrderCommandHandler(commandGateway, queryGateway);
        var command = new CreateOrderRequestCommand(List.of(new OrderLine("product-1", 3)));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(InsufficientInventoryException.class);
        verify(commandGateway, never()).sendAndWait(any(CreateOrderCommand.class));
    }
}
