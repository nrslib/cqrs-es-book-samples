package org.example.cqrsesbooksamples.chapter03._65;

public class UserReadModel {
    private final String userId;
    private String name;
    private String email;

    public UserReadModel(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void updateName(String name) { this.name = name; }
    public void updateEmail(String email) { this.email = email; }
}
