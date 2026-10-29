package org.example.cqrsesbooksamples.chapter02._72;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users_72")
public class UserReadModel {
    @Id
    private String userId;
    private String fullName;
    private String email;

    protected UserReadModel() {}

    public UserReadModel(String userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
}
