package org.example.cqrsesbooksamples.chapter03._33;

public record RegistrationRequest(
    String email,
    String phoneNumber,
    String address,
    String displayName
) {}
