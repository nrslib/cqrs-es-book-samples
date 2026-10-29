package org.example.cqrsesbooksamples.chapter03._33;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users_33")
public class UserProjectionEntity {
    @Id
    private String userId;
    private String personalInfoId;
    private String displayName;
    private Instant createdAt;

    protected UserProjectionEntity() {}

    public UserProjectionEntity(String userId, String personalInfoId, String displayName, Instant createdAt) {
        this.userId = userId;
        this.personalInfoId = personalInfoId;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }

    public String getUserId() { return userId; }
    public String getPersonalInfoId() { return personalInfoId; }
    public String getDisplayName() { return displayName; }
    public Instant getCreatedAt() { return createdAt; }
}
