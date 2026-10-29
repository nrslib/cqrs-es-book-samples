package org.example.cqrsesbooksamples.chapter04._37.commands;

import org.example.cqrsesbooksamples.chapter04._37.TransferId;

public record FailTransferCommand(TransferId transferId, String reason) {}
