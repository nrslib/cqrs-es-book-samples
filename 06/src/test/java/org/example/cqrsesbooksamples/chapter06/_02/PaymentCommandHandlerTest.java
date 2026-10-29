package org.example.cqrsesbooksamples.chapter06._02;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter06._01.AccountData;
import org.example.cqrsesbooksamples.chapter06._01.GetAccountQuery;
import org.example.cqrsesbooksamples.chapter06._01.GetAccountQueryResult;
import org.example.cqrsesbooksamples.chapter06._01.NotFoundException;
import org.example.cqrsesbooksamples.chapter06._01.PaymentMethod;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessId;
import org.example.cqrsesbooksamples.chapter06._01.StartPaymentCommand;
import org.junit.jupiter.api.Test;

/**
 * 口座の存在確認を挟む外部コマンドハンドラ（旧 6.2）の検証。
 * 口座があれば決済開始コマンドを送り、なければ NotFoundException で
 * 弾いてコマンドを送らないことを確かめる。
 */
class PaymentCommandHandlerTest {

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final QueryGateway queryGateway = mock(QueryGateway.class);
    private final PaymentCommandHandler handler =
            new PaymentCommandHandler(commandGateway, queryGateway);

    @Test
    void 口座が存在すれば決済開始コマンドを送信する() {
        var accountId = UUID.randomUUID();
        when(queryGateway.query(eq(new GetAccountQuery(accountId)), eq(GetAccountQueryResult.class)))
                .thenReturn(CompletableFuture.completedFuture(new GetAccountQueryResult(
                        Optional.of(new AccountData(accountId, "John")))));
        var processId = new PaymentProcessId();
        when(commandGateway.sendAndWait(any(StartPaymentCommand.class))).thenReturn(processId);

        var result = handler.handle(new RequestPaymentCommand(
                accountId, new BigDecimal("5000"), PaymentMethod.CREDIT));

        assertThat(result).isEqualTo(processId);
        verify(commandGateway).sendAndWait(any(StartPaymentCommand.class));
    }

    @Test
    void 口座が存在しなければ決済開始コマンドは送らない() {
        var accountId = UUID.randomUUID();
        when(queryGateway.query(eq(new GetAccountQuery(accountId)), eq(GetAccountQueryResult.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        new GetAccountQueryResult(Optional.empty())));

        assertThatThrownBy(() -> handler.handle(new RequestPaymentCommand(
                accountId, new BigDecimal("5000"), PaymentMethod.CREDIT)))
                .isInstanceOf(NotFoundException.class);
        verify(commandGateway, never()).sendAndWait(any(StartPaymentCommand.class));
    }
}
