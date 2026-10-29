// 紙面照合: 6.21
package org.example.cqrsesbooksamples.chapter06._26;

import java.time.LocalDate;


public record TodoListCreatedEvent(String todoListId, LocalDate date) {}
