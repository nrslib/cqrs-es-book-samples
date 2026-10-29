package org.example.cqrsesbooksamples.chapter03._04;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users_v2")
public class UserReadModelV2 {
    @Id
    private String userId;
    private String fullName;
    private String email;
    private Instant registeredAt;

    protected UserReadModelV2() {}

    public UserReadModelV2(String userId, String fullName, String email, Instant registeredAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.registeredAt = registeredAt;
    }

    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Instant getRegisteredAt() { return registeredAt; }
}
