// 紙面照合: 3.7
package org.example.cqrsesbooksamples.chapter03._11;

import org.axonframework.commandhandling.CommandBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MaintenanceModeConfig {
    @Value("${maintenance.mode:false}")
    private boolean maintenanceMode;

    @Autowired
    public void registerInterceptor(CommandBus commandBus) {
        commandBus.registerDispatchInterceptor(messages -> (index, command) -> {
            if (maintenanceMode) {
                throw new MaintenanceModeException(
                    "システムはメンテナンス中です。しばらくお待ちください。");
            }
            return command;
        });
    }
}
