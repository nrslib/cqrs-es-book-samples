// 紙面照合: 2.10
package org.example.cqrsesbooksamples.chapter02._14;


public sealed interface UserEvent permits UserCreatedEvent, UserUpdatedEvent, UserDeletedEvent {
    String userId();
}
