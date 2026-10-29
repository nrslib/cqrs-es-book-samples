package org.example.cqrsesbooksamples.chapter02._75;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class NotificationService {
    private final List<String> sentEmails = new ArrayList<>();

    public void sendWelcomeEmail(String email) {
        sentEmails.add(email);
    }

    public List<String> sentEmails() {
        return List.copyOf(sentEmails);
    }
}
