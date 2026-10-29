// 紙面照合: 3.48
package org.example.cqrsesbooksamples.chapter03._65;

import org.axonframework.eventhandling.EventHandler;

public class IdempotentUserProjection {
    private final UserReadModelRepository repository;

    public IdempotentUserProjection(UserReadModelRepository repository) {
        this.repository = repository;
    }


    @EventHandler
    public void on(UserRegisteredEvent event) {
        // べき等性を保証：すでに存在する場合は更新、存在しない場合は作成
        var model = repository.findById(event.userId())
            .orElse(new UserReadModel(event.userId()));

        model.updateName(event.name());
        model.updateEmail(event.email());
        repository.save(model);
    }
}
