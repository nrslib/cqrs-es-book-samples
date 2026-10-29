package org.example.cqrsesbooksamples.chapter06._34;

import java.time.LocalDate;
import java.util.List;

// 書籍6.26と同じ定義（createNew版パッケージ用の再掲。正は _26 側）
public record RegisterTodoListCommand(LocalDate date, List<String> itemTexts) {}
