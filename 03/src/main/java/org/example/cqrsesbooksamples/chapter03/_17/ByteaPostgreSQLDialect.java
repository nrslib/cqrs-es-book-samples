// 紙面照合: 3.22
package org.example.cqrsesbooksamples.chapter03._17;

import java.sql.Types;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;


public class ByteaPostgreSQLDialect extends PostgreSQLDialect {

    public ByteaPostgreSQLDialect() {
        super(DatabaseVersion.make(14, 0));
    }

    @Override
    protected String columnType(int sqlTypeCode) {
        if (sqlTypeCode == SqlTypes.BLOB || sqlTypeCode == Types.BLOB) {
            return "bytea";
        }
        if (sqlTypeCode == SqlTypes.CLOB || sqlTypeCode == Types.CLOB) {
            return "text";
        }
        return super.columnType(sqlTypeCode);
    }

    // castType(int) も columnType と同じ対応で上書きする
    @Override
    protected String castType(int sqlTypeCode) {
        if (sqlTypeCode == SqlTypes.BLOB || sqlTypeCode == Types.BLOB) {
            return "bytea";
        }
        if (sqlTypeCode == SqlTypes.CLOB || sqlTypeCode == Types.CLOB) {
            return "text";
        }
        return super.castType(sqlTypeCode);
    }

    @Override
    public void contributeTypes(
            TypeContributions typeContributions,
            ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        typeContributions.getTypeConfiguration().getJdbcTypeRegistry()
            .addDescriptor(Types.BLOB, BinaryJdbcType.INSTANCE);
    }
}
