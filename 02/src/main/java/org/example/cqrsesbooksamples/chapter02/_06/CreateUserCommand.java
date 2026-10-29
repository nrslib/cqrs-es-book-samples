package org.example.cqrsesbooksamples.chapter02._6;

// 削除済み（旧 2.6）
public class CreateUserCommand {
    private String name;
    private String email;

    public CreateUserCommand(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
