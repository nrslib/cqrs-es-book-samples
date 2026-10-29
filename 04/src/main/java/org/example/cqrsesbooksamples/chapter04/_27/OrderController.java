// 紙面照合: 4.23
package org.example.cqrsesbooksamples.chapter04._27;

import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter04._20.GetInventoryQuery;
import org.example.cqrsesbooksamples.chapter04._20.InventoryQueryResult;
import org.example.cqrsesbooksamples.chapter04._29.CreateOrderCommand;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/orders")
public class OrderController {
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public OrderController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    public CompletableFuture<String> createOrder(@RequestBody CreateOrderRequest request) {
        // すべての注文明細の在庫を確認
        var inventoryQueries = request.orderLines().stream()
            .map(line -> new GetInventoryQuery(line.productId()))
            .map(query -> queryGateway.query(query, InventoryQueryResult.class))
            .toList();

        // すべての在庫確認が完了するのを待つ
        CompletableFuture.allOf(inventoryQueries.toArray(CompletableFuture[]::new))
            .thenAccept(v -> {
                for (int i = 0; i < inventoryQueries.size(); i++) {
                    var inventory = inventoryQueries.get(i).join();
                    var line = request.orderLines().get(i);
                    if (inventory.quantity() < line.quantity()) {
                        throw new InsufficientInventoryException(line.productId());
                    }
                }
            }).join();

        // 在庫が十分にある場合、注文作成コマンドを発行
        var command = new CreateOrderCommand(request.orderLines());
        return commandGateway.send(command);
    }
}
