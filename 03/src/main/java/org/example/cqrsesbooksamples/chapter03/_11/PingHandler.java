package org.example.cqrsesbooksamples.chapter03._11;

import org.axonframework.commandhandling.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class PingHandler {
    @CommandHandler
    public String handle(PingCommand command) {
        return "pong";
    }
}
