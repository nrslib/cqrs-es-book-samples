// 紙面照合: 2.26
package org.example.cqrsesbooksamples.chapter02._2;

import org.axonframework.modelling.command.TargetAggregateIdentifier;


public record UpdateUserCommand(
    @TargetAggregateIdentifier String userId,
    String name,
    String email
) {}
