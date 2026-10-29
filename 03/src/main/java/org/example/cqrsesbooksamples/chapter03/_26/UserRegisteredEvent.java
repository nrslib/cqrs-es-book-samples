// 紙面照合: 3.32
package org.example.cqrsesbooksamples.chapter03._26;

import org.axonframework.serialization.Revision;


@Revision("1")
public record UserRegisteredEvent(
    String userId,
    String name,
    String email
) {}
