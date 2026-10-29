// 紙面照合: 6.22
package org.example.cqrsesbooksamples.chapter06._26;


public record TodoItemCreationRequestedEvent(String todoListId, String itemId, String text) {}
