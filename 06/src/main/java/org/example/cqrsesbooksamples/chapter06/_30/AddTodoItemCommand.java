package org.example.cqrsesbooksamples.chapter06._30;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

// 書籍6.26と同じ定義（修正版パッケージ用の再掲。正は _26 側）
public record AddTodoItemCommand(@TargetAggregateIdentifier String todoListId, String text) {}
