// 紙面照合: 4.33
package org.example.cqrsesbooksamples.chapter04._37;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._34.Money;
import org.example.cqrsesbooksamples.chapter04._37.commands.CompleteTransferCommand;
import org.example.cqrsesbooksamples.chapter04._37.commands.CreditAccountCommand;
import org.example.cqrsesbooksamples.chapter04._37.commands.DebitAccountCommand;
import org.example.cqrsesbooksamples.chapter04._37.commands.FailTransferCommand;
import org.example.cqrsesbooksamples.chapter04._37.commands.RefundAccountCommand;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountCreditFailedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountCreditedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountDebitFailedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountDebitedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountRefundedEvent;

import jakarta.inject.Inject;


@Saga
public class TransferProcessingSaga {
    @Inject
    private transient CommandGateway commandGateway;

    private TransferId transferId;
    private AccountId sourceAccountId;
    private AccountId targetAccountId;
    private Money amount;
    private boolean sourceDebited = false;
    private String creditFailureReason;

    @StartSaga
    @SagaEventHandler(associationProperty = "transferId")
    public void on(TransferRegisteredEvent event) {
        this.transferId = event.transferId();
        this.sourceAccountId = event.sourceAccountId();
        this.targetAccountId = event.targetAccountId();
        this.amount = event.amount();

        // まず送金元から引き落とし
        commandGateway.send(new DebitAccountCommand(
            sourceAccountId, amount, transferId));
    }

    @SagaEventHandler(associationProperty = "transferId")
    public void on(AccountDebitedEvent event) {
        sourceDebited = true;

        // 引き落とし成功後、送金先に入金
        commandGateway.send(new CreditAccountCommand(
            targetAccountId, amount, transferId));
    }

    @SagaEventHandler(associationProperty = "transferId")
    public void on(AccountCreditedEvent event) {
        // 両方成功したら送金完了
        commandGateway.send(
            new CompleteTransferCommand(transferId));
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "transferId")
    public void on(AccountDebitFailedEvent event) {
        // 引き落としが失敗したので、送金失敗として記録する
        commandGateway.send(new FailTransferCommand(
            transferId, event.reason()));
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "transferId")
    public void on(AccountCreditFailedEvent event) {
        // 入金が失敗したので、引き落とし済みなら返金し、その記録を待つ
        if (sourceDebited) {
            creditFailureReason = event.reason();
            commandGateway.send(new RefundAccountCommand(
                sourceAccountId, amount, transferId));
            return;
        }
        commandGateway.send(new FailTransferCommand(
            transferId, "入金失敗: " + event.reason()));
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "transferId")
    public void on(AccountRefundedEvent event) {
        // 返金の記録を受けて送金失敗を確定
        commandGateway.send(new FailTransferCommand(
            transferId, "入金失敗: " + creditFailureReason));
        SagaLifecycle.end();
    }
}
