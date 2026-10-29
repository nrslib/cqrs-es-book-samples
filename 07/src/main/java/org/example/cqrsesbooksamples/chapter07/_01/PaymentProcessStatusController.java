package org.example.cqrsesbooksamples.chapter07._01;

import org.example.cqrsesbooksamples.chapter07.support.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 削除済み（旧 7.3）
@RestController
@RequestMapping("/api/payments/status")
public class PaymentProcessStatusController {
    private final PaymentProcessStatusRepository repository;

    public PaymentProcessStatusController(PaymentProcessStatusRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{processId}")
    public PaymentProcessStatusResponse getStatus(@PathVariable String processId) {
        var status = repository.findById(processId)
            .orElseThrow(() -> new NotFoundException("Process not found"));
        return new PaymentProcessStatusResponse(
            status.getProcessId(),
            status.getCurrentState(),
            status.getLastUpdated()
        );
    }
}
