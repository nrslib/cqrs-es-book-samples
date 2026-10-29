// 検証補助: 1.2
package org.example.cqrsesbooksamples.chapter01._02;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

/**
 * 業務データと送信用メッセージを単一トランザクションで保存するOutboxパターンを検証する。
 */
class OutboxPatternTest {

    @Test
    void 注文の保存とOutboxへの保存が単一トランザクションで確定する() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:chapter01_outbox")) {
            try (var stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE orders (id VARCHAR(36) PRIMARY KEY, customer_id VARCHAR(36))");
                stmt.execute("CREATE TABLE outbox (message_id VARCHAR(36) PRIMARY KEY, "
                    + "message_type VARCHAR(100), payload VARCHAR(1000))");
            }

            // BEGIN TRANSACTION 〜 COMMIT: 注文とOutboxメッセージを同時に保存する
            conn.setAutoCommit(false);
            try (var stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO orders (id, customer_id) VALUES ('order-1', 'customer-1')");
                stmt.execute("INSERT INTO outbox (message_id, message_type, payload) "
                    + "VALUES ('msg-1', 'OrderPlaced', '{\"orderId\":\"order-1\"}')");
            }
            conn.commit();

            assertThat(count(conn, "orders")).isEqualTo(1);
            assertThat(count(conn, "outbox")).isEqualTo(1);

            // 途中で失敗したらロールバックされ、注文だけ・メッセージだけの状態は残らない
            try (var stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO orders (id, customer_id) VALUES ('order-2', 'customer-2')");
            }
            conn.rollback();

            assertThat(count(conn, "orders")).isEqualTo(1);
            assertThat(count(conn, "outbox")).isEqualTo(1);
        }
    }

    private int count(Connection conn, String table) throws SQLException {
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
