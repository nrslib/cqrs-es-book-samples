// 紙面照合: 3.26
package org.example.cqrsesbooksamples.chapter03._21.v1;


// Version 1.0
public record UserRegisteredEvent(
    String userId,
    String email,
    String password
) {}
