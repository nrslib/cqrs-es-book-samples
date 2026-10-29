// 紙面照合: 6.21
package org.example.cqrsesbooksamples.chapter06._26;

import java.time.LocalDate;
import java.util.List;


public record RegisterTodoListCommand(LocalDate date, List<String> itemTexts) {}
