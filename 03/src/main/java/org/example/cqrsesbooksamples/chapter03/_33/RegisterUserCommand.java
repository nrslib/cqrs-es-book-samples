package org.example.cqrsesbooksamples.chapter03._33;

public record RegisterUserCommand(
    String userId,
    String personalInfoId,
    String displayName
) {}
