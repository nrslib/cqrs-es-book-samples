package org.example.cqrsesbooksamples.chapter03._35;

public record UserRegisteredEvent(
    String userId,
    String keyReference,
    String encryptedData
) {}
