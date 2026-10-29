// 紙面照合: 4.24
package org.example.cqrsesbooksamples.chapter04._28;

import java.util.List;

import org.example.cqrsesbooksamples.chapter04._29.OrderLine;


public record CreateOrderRequestCommand(List<OrderLine> orderLines) {}
