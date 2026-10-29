// 検証補助: 3.16
package org.example.cqrsesbooksamples.chapter03._50;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;


class SegmentOwnershipSqlTest {

    @Test
    void プロセッサが所有するセグメントとインスタンスを取得できる() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:chapter03_50")) {
            try (var statement = connection.createStatement()) {
                statement.execute("""
                    CREATE TABLE token_entry (
                        processor_name VARCHAR(255) NOT NULL,
                        segment INTEGER NOT NULL,
                        owner VARCHAR(255),
                        PRIMARY KEY (processor_name, segment)
                    )
                    """);
                statement.executeUpdate("""
                    INSERT INTO token_entry (processor_name, segment, owner) VALUES
                        ('user-projection', 0, 'instance-1'),
                        ('user-projection', 1, 'instance-2'),
                        ('other-projection', 0, 'instance-3')
                    """);
            }

            var owners = new ArrayList<SegmentOwner>();
            var query = Files.readString(Path.of("snippets/segment-ownership.sql"));
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    owners.add(new SegmentOwner(
                        resultSet.getInt("segment"),
                        resultSet.getString("owner")
                    ));
                }
            }

            assertThat(owners).containsExactlyInAnyOrder(
                new SegmentOwner(0, "instance-1"),
                new SegmentOwner(1, "instance-2")
            );
        }
    }

    private record SegmentOwner(int segment, String owner) {}
}
