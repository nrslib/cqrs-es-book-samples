// 紙面照合: 4.24
package org.example.cqrsesbooksamples.chapter04._28;

import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter04._20.GetInventoryQuery;
import org.example.cqrsesbooksamples.chapter04._20.InventoryQueryResult;
import org.example.cqrsesbooksamples.chapter04._27.InsufficientInventoryException;
import org.example.cqrsesbooksamples.chapter04._29.CreateOrderCommand;
import org.springframework.stereotype.Component;


@Component
public class OrderCommandHandler {
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public OrderCommandHandler(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @CommandHandler
    public String handle(CreateOrderRequestCommand command) {
        // すべての注文明細の在庫を確認
        var inventoryQueries = command.orderLines().stream()
            .map(line -> new GetInventoryQuery(line.productId()))
            .map(query -> queryGateway.query(query, InventoryQueryResult.class))
            .toList();

        // すべての在庫確認が完了するのを待つ
        CompletableFuture.allOf(inventoryQueries.toArray(CompletableFuture[]::new))
            .thenAccept(v -> {
                for (int i = 0; i < inventoryQueries.size(); i++) {
                    var inventory = inventoryQueries.get(i).join();
                    var line = command.orderLines().get(i);
                    if (inventory.quantity() < line.quantity()) {
                        throw new InsufficientInventoryException(line.productId());
                    }
                }
            }).join();

        // 在庫が十分にある場合、注文作成コマンドを発行
        return commandGateway.sendAndWait(
            new CreateOrderCommand(command.orderLines()));
    }
}
