package org.example.cqrsesbooksamples.chapter02._75;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users_75")
public class UserReadModel {
    @Id
    private String userId;
    private String name;
    private String email;

    protected UserReadModel() {}

    public UserReadModel(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
