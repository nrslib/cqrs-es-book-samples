package org.example.cqrsesbooksamples.chapter03._35;

public record RegisterUserCommand(
    String userId,
    String keyReference,
    PersonalData personalData
) {}
