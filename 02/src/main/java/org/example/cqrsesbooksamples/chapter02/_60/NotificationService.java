package org.example.cqrsesbooksamples.chapter02._60;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

/**
 * メール送信を模したサービス。
 * 送信時点でリードモデルが存在したかを記録し、@Order による実行順序の検証に使う。
 */
@Component
public class NotificationService {
    private final UserReadModelRepository repository;
    private final List<String> sentEmails = new CopyOnWriteArrayList<>();
    private final AtomicBoolean readModelExistedAtSend = new AtomicBoolean(true);

    public NotificationService(UserReadModelRepository repository) {
        this.repository = repository;
    }

    public void sendWelcomeEmail(String email) {
        if (repository.count() == 0) {
            readModelExistedAtSend.set(false);
        }
        sentEmails.add(email);
    }

    public List<String> sentEmails() {
        return List.copyOf(sentEmails);
    }

    public boolean readModelExistedAtSend() {
        return readModelExistedAtSend.get();
    }
}
