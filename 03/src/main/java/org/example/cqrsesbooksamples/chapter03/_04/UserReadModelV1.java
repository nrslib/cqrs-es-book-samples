package org.example.cqrsesbooksamples.chapter03._04;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users_v1")
public class UserReadModelV1 {
    @Id
    private String userId;
    private String name;
    private String email;

    protected UserReadModelV1() {}

    public UserReadModelV1(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
