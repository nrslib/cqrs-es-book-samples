// 検証補助: 4.31
package org.example.cqrsesbooksamples.chapter04._34;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
    classes = Chapter04_34TestApplication.class,
    properties = "spring.datasource.url=jdbc:h2:mem:chapter04_34_tx;DB_CLOSE_DELAY=-1"
)
@Import(MoneyTransferTransactionIntegrationTest.RepositoryConfig.class)
class MoneyTransferTransactionIntegrationTest {
    @Autowired
    private MoneyTransferApplicationService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcAccountRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS accounts");
        jdbcTemplate.execute("""
            CREATE TABLE accounts (
                account_id VARCHAR PRIMARY KEY,
                balance DECIMAL(19, 2) NOT NULL
            )
            """);
        jdbcTemplate.update("INSERT INTO accounts VALUES (?, ?)", "A", new BigDecimal("1000"));
        jdbcTemplate.update("INSERT INTO accounts VALUES (?, ?)", "B", new BigDecimal("500"));
        repository.failSavingTargetAccount = false;
    }

    @Test
    void 同じ値の口座IDへの送金を拒否して残高を変えない() {
        assertAll(
            () -> assertThatThrownBy(() -> service.transfer(
                new AccountId("A"), new AccountId("A"), new Money(new BigDecimal("300"))))
                .isInstanceOf(IllegalArgumentException.class),
            () -> assertThat(balanceOf("A")).isEqualByComparingTo("1000"));
    }

    @Test
    void 別口座への送金で合計残高を保ったまま両口座を更新する() {
        service.transfer(new AccountId("A"), new AccountId("B"), new Money(new BigDecimal("300")));

        assertThat(balanceOf("A")).isEqualByComparingTo("700");
        assertThat(balanceOf("B")).isEqualByComparingTo("800");
    }

    @Test
    void 送金先の保存に失敗すると送金元の更新もロールバックされる() {
        repository.failSavingTargetAccount = true;

        assertThatThrownBy(() -> service.transfer(
            new AccountId("A"), new AccountId("B"), new Money(new BigDecimal("300"))))
            .isInstanceOf(IllegalStateException.class);

        assertThat(balanceOf("A")).isEqualByComparingTo("1000");
        assertThat(balanceOf("B")).isEqualByComparingTo("500");
    }

    @Test
    void 送金元が存在しなければ送金先の残高を変えない() {
        assertThatThrownBy(() -> service.transfer(
            new AccountId("missing"), new AccountId("B"), new Money(new BigDecimal("300"))))
            .isInstanceOf(AccountNotFoundException.class);

        assertThat(balanceOf("B")).isEqualByComparingTo("500");
    }

    @Test
    void 送金先が存在しなければ送金元の残高を変えない() {
        assertThatThrownBy(() -> service.transfer(
            new AccountId("A"), new AccountId("missing"), new Money(new BigDecimal("300"))))
            .isInstanceOf(AccountNotFoundException.class);

        assertThat(balanceOf("A")).isEqualByComparingTo("1000");
    }

    private BigDecimal balanceOf(String accountId) {
        return jdbcTemplate.queryForObject(
            "SELECT balance FROM accounts WHERE account_id = ?", BigDecimal.class, accountId);
    }

    @TestConfiguration
    static class RepositoryConfig {
        @Bean
        JdbcAccountRepository accountRepository(JdbcTemplate jdbcTemplate) {
            return new JdbcAccountRepository(jdbcTemplate);
        }
    }

    static class JdbcAccountRepository implements AccountRepository {
        private final JdbcTemplate jdbcTemplate;
        private boolean failSavingTargetAccount;

        JdbcAccountRepository(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Override
        public Optional<Account> findById(AccountId id) {
            return jdbcTemplate.query(
                "SELECT balance FROM accounts WHERE account_id = ?",
                (resultSet, rowNumber) -> new Account(
                    id, new Money(resultSet.getBigDecimal("balance"))),
                id.value()
            ).stream().findFirst();
        }

        @Override
        public Account save(Account account) {
            if (failSavingTargetAccount && account.getId().equals(new AccountId("B"))) {
                throw new IllegalStateException("Target account save failed");
            }
            jdbcTemplate.update(
                "UPDATE accounts SET balance = ? WHERE account_id = ?",
                account.getBalance().amount(), account.getId().value());
            return account;
        }
    }
}
