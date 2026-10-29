// 検証補助: 4.25
package org.example.cqrsesbooksamples.chapter04._29;

import java.util.List;


public record CreateOrderCommand(List<OrderLine> orderLines) {}
