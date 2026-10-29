// 紙面照合: 2.10
package org.example.cqrsesbooksamples.chapter02._14;


public record UserUpdatedEvent(String userId, String name, String email) implements UserEvent { }
