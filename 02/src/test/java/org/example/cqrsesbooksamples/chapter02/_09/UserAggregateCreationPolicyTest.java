// 検証補助: 2.7
package org.example.cqrsesbooksamples.chapter02._9;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.Test;

class UserAggregateCreationPolicyTest {

    @Test
    void CreationPolicyを使うとコマンドハンドラが任意の戻り値を返せる() {
        var fixture = new AggregateTestFixture<>(UserAggregate.class);
        fixture.givenNoPriorActivity()
            .when(new CreateUserCommand("John", "john@example.com"))
            .expectSuccessfulHandlerExecution()
            .expectResultMessagePayloadMatching(Matchers.predicate(
                (CreateUserResult result) ->
                    result.userId() != null
                        && result.name().equals("John")
                        && result.email().equals("john@example.com")));
    }
}
