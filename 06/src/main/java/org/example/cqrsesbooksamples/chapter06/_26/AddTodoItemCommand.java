// 紙面照合: 6.21
package org.example.cqrsesbooksamples.chapter06._26;

import org.axonframework.modelling.command.TargetAggregateIdentifier;


public record AddTodoItemCommand(@TargetAggregateIdentifier String todoListId, String text) {}
