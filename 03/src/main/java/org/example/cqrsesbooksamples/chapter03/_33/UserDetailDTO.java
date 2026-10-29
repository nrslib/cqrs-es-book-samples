package org.example.cqrsesbooksamples.chapter03._33;

import java.time.Instant;

public record UserDetailDTO(
    String userId,
    String displayName,
    String email,
    String phoneNumber,
    String address,
    Instant createdAt
) {}
