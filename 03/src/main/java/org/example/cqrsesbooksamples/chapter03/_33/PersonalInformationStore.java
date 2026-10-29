package org.example.cqrsesbooksamples.chapter03._33;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PersonalInformationStore {
    private final JdbcTemplate jdbcTemplate;
    private final EncryptionService encryptionService;

    public PersonalInformationStore(JdbcTemplate jdbcTemplate,
                                   EncryptionService encryptionService) {
        this.jdbcTemplate = jdbcTemplate;
        this.encryptionService = encryptionService;
    }

    public String store(PersonalInformation info) {
        var id = UUID.randomUUID().toString();
        var encrypted = encryptionService.encrypt(info);

        jdbcTemplate.update(
            "INSERT INTO personal_information (id, email, phone_number, address, created_at) VALUES (?, ?, ?, ?, ?)",
            id,
            encrypted.email(),
            encrypted.phoneNumber(),
            encrypted.address(),
            Instant.now()
        );

        return id;
    }

    public Optional<PersonalInformation> retrieve(String id) {
        var result = jdbcTemplate.query(
            "SELECT email, phone_number, address FROM personal_information WHERE id = ?",
            (rs, rowNum) -> new EncryptedPersonalInformation(
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("address")
            ),
            id
        );

        return result.isEmpty()
            ? Optional.empty()
            : Optional.of(encryptionService.decrypt(result.get(0)));
    }

    public void delete(String id) {
        // 参照IDパターンの狙いどおり、外部ストア側は本当に消す（物理削除）
        jdbcTemplate.update(
            "DELETE FROM personal_information WHERE id = ?",
            id
        );
    }

    public void update(String id, PersonalInformation info) {
        var encrypted = encryptionService.encrypt(info);

        jdbcTemplate.update(
            "UPDATE personal_information SET email = ?, phone_number = ?, address = ?, updated_at = ? WHERE id = ?",
            encrypted.email(),
            encrypted.phoneNumber(),
            encrypted.address(),
            Instant.now(),
            id
        );
    }
}
