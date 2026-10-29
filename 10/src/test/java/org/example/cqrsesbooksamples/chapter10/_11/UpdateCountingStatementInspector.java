package org.example.cqrsesbooksamples.chapter10._11;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.resource.jdbc.spi.StatementInspector;

// document_viewsへのUPDATE文の発行回数を数える（まとめ書きの検証用）。
// FAIL_NEXT_UPDATEを立てると次のUPDATEを1回だけ失敗させ、一時的な障害を注入できる。
public class UpdateCountingStatementInspector implements StatementInspector {
    static final AtomicInteger UPDATE_COUNT = new AtomicInteger();
    static final AtomicBoolean FAIL_NEXT_UPDATE = new AtomicBoolean(false);

    @Override
    public String inspect(String sql) {
        String lower = sql.toLowerCase();
        if (lower.startsWith("update") && lower.contains("document_views")) {
            if (FAIL_NEXT_UPDATE.compareAndSet(true, false)) {
                throw new RuntimeException("一時的な障害を注入");
            }
            UPDATE_COUNT.incrementAndGet();
        }
        return sql;
    }
}
