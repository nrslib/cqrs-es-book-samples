// 紙面照合: 7.16
package org.example.cqrsesbooksamples.chapter07._19;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @PostMapping
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        // 支払い処理の実行
        return new PaymentResponse(UUID.randomUUID().toString(), "ACCEPTED");
    }
}
