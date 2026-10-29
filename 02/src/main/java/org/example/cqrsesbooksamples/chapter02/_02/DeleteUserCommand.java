// 紙面照合: 2.35
package org.example.cqrsesbooksamples.chapter02._2;

import org.axonframework.modelling.command.TargetAggregateIdentifier;


public record DeleteUserCommand(
    @TargetAggregateIdentifier String userId
) {}
