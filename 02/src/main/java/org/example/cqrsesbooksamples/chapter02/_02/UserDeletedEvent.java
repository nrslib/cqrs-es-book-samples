// 紙面照合: 2.39
package org.example.cqrsesbooksamples.chapter02._2;


public record UserDeletedEvent(
    String userId
) implements UserEvent {
}
