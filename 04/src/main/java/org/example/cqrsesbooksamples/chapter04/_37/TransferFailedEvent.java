package org.example.cqrsesbooksamples.chapter04._37;

public record TransferFailedEvent(TransferId transferId, String reason) implements TransferEvent {}
