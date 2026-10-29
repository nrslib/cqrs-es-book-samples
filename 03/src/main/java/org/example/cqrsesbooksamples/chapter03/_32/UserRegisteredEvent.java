// 紙面照合: 3.36
package org.example.cqrsesbooksamples.chapter03._32;

import java.time.LocalDate;


public record UserRegisteredEvent(
    String userId,
    String name,
    String email,
    String phoneNumber,
    String address,
    LocalDate birthDate
) {}
