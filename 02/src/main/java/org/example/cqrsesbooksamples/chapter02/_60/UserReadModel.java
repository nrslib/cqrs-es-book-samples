package org.example.cqrsesbooksamples.chapter02._60;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserReadModel {
    @Id
    private String userId;
    private String name;

    protected UserReadModel() {}

    public UserReadModel(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
}
