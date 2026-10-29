// 紙面照合: 8.11, 8.12, 8.13, 8.14
package org.example.cqrsesbooksamples.chapter08._11;

import java.time.Duration;

import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class OrderProcessSagaTest {
    private SagaTestFixture<OrderProcessSaga> fixture;

    @BeforeEach
    void setUp() {
        fixture = new SagaTestFixture<>(OrderProcessSaga.class);
    }


    @Test
    void shouldStartSagaOnOrderCreated() {
        fixture.givenNoPriorActivity()
               .whenPublishingA(new OrderCreatedEvent("ORDER-001", 1000))
               .expectActiveSagas(1)
               .expectDispatchedCommands(new ProcessPaymentCommand("ORDER-001", 1000));
    }

    @Test
    void shouldShipOrderOnPaymentCompleted() {
        fixture.givenAPublished(new OrderCreatedEvent("ORDER-001", 1000))
               .whenPublishingA(new PaymentCompletedEvent("ORDER-001"))
               .expectActiveSagas(1)
               .expectDispatchedCommands(new ShipOrderCommand("ORDER-001"));
    }

    @Test
    void shouldEndSagaOnOrderShipped() {
        fixture.givenAPublished(new OrderCreatedEvent("ORDER-001", 1000))
               .andThenAPublished(new PaymentCompletedEvent("ORDER-001"))
               .whenPublishingA(new OrderShippedEvent("ORDER-001"))
               .expectActiveSagas(0);
    }


    @Test
    void shouldHandleTimeout() {
        fixture.givenAPublished(new OrderCreatedEvent("ORDER-001", 1000))
               .whenTimeElapses(Duration.ofMinutes(30))
               .expectDispatchedCommands(new CancelOrderCommand("ORDER-001"))
               .expectActiveSagas(0);
    }


    @Test
    void shouldNotTimeoutBefore30Minutes() {
        fixture.givenAPublished(new OrderCreatedEvent("ORDER-001", 1000))
               .whenTimeElapses(Duration.ofMinutes(29))
               .expectActiveSagas(1);
    }

    @Test
    void shouldNotCancelPaidOrderAfterPaymentDeadline() throws Exception {
        fixture.givenAPublished(new OrderCreatedEvent("ORDER-001", 1000))
               .andThenTimeElapses(Duration.ofMinutes(29))
               .andThenAPublished(new PaymentCompletedEvent("ORDER-001"))
               .whenTimeElapses(Duration.ofMinutes(2))
               .expectNoDispatchedCommands()
               .expectActiveSagas(1);
    }


    @Test
    void shouldExecuteCompensatingActions() {
        fixture.givenAPublished(new OrderCreatedEvent("ORDER-001", 1000))
               .whenPublishingA(new PaymentFailedEvent("ORDER-001"))
               .expectDispatchedCommands(
                   new ReleaseInventoryCommand("ORDER-001"),
                   new NotifyCustomerOfFailureCommand("ORDER-001")
               )
               .expectActiveSagas(0);
    }
}
