package org.example.cqrsesbooksamples.chapter04._27;

import java.util.List;

import org.example.cqrsesbooksamples.chapter04._29.OrderLine;

public record CreateOrderRequest(List<OrderLine> orderLines) {}
