package org.example.cqrsesbooksamples.chapter03._33;

public record PersonalInfoUpdateRequest(
    String email,
    String phoneNumber,
    String address
) {}
