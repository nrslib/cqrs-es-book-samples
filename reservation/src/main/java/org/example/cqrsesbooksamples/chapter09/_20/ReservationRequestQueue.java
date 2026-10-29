// 紙面照合: 9.17

package org.example.cqrsesbooksamples.chapter09._20;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestReservationCommand;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ReservationRequestQueue {

    private final Queue<RequestReservationCommand> pendingCommands =
        new ConcurrentLinkedQueue<>();
    private final CommandGateway commandGateway;

    public ReservationRequestQueue(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public void enqueue(RequestReservationCommand command) {
        pendingCommands.add(command);
    }

    // 一定間隔で1件ずつコマンドを送る
    @Scheduled(fixedDelayString = "${reservation.ingress.interval-ms:100}")
    void dispatchNext() {
        // 送信が成功するまでキューから取り除かない。
        // 失敗したら次の周期に同じ要求を再送する
        var command = pendingCommands.peek();
        if (command != null) {
            commandGateway.sendAndWait(command);
            pendingCommands.remove();
        }
    }
}
