// 紙面照合: 2.19
package org.example.cqrsesbooksamples.chapter02._2;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "users")
public class UserDataModel {
    @Id
    private String userId;  // ユーザID（主キー）
    private String name;    // ユーザ名
    private String email;   // メールアドレス

    protected UserDataModel() {}  // JPAのために必要な引数なしコンストラクタ

    public UserDataModel(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    // ゲッターメソッド
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    // セッターメソッド
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
}
