package org.example.cqrsesbooksamples.chapter06._34;

// 書籍6.30と同じ定義（createNew版パッケージ用の再掲。正は _30 側）
public record TodoListItemAddedEvent(String todoListId, String itemId, String text) {}
