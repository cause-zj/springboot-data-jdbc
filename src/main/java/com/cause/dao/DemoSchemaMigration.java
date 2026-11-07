package com.cause.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * 给已经建过的 {@code demo_item} 补齐后续案例用到的列、索引和外键。
 * 新库的 {@code schema.sql} 已经是完整结构，这里发现列存在就跳过。
 */
@Component
public class DemoSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoSchemaMigration.class);

    private final JdbcTemplate jdbc;

    public DemoSchemaMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        addColumnIfMissing("category_id", "BIGINT NULL");
        addColumnIfMissing("amount", "DECIMAL(12, 2) NOT NULL DEFAULT 0");
        addColumnIfMissing("remark", "VARCHAR(200) NULL");
        addColumnIfMissing("update_time", "TIMESTAMP NULL");
        backfillSeedRows();
        addIndexIfMissing();
        addForeignKeyIfMissing();
    }

    private void addColumnIfMissing(String column, String definition) {
        if (columnExists("demo_item", column)) {
            return;
        }
        jdbc.execute("ALTER TABLE demo_item ADD COLUMN " + column + " " + definition);
        log.info("已补充 demo_item.{}", column);
    }

    private void backfillSeedRows() {
        int first = jdbc.update("""
                UPDATE demo_item
                   SET category_id = 1,
                       amount = 12.50,
                       remark = '种子数据'
                 WHERE id = 1
                   AND category_id IS NULL
                """);
        int second = jdbc.update("""
                UPDATE demo_item
                   SET category_id = 2,
                       amount = 8.00,
                       remark = '种子数据'
                 WHERE id = 2
                   AND category_id IS NULL
                """);
        if (first + second > 0) {
            log.info("已回填 demo_item 种子行 {} 条", first + second);
        }
    }

    private void addIndexIfMissing() {
        if (indexExists("demo_item", "idx_demo_item_category")) {
            return;
        }
        jdbc.execute("CREATE INDEX idx_demo_item_category ON demo_item (category_id)");
        log.info("已补充索引 idx_demo_item_category");
    }

    private void addForeignKeyIfMissing() {
        if (foreignKeyExists("demo_item", "fk_demo_item_category")) {
            return;
        }
        jdbc.execute("""
                ALTER TABLE demo_item
                ADD CONSTRAINT fk_demo_item_category
                FOREIGN KEY (category_id) REFERENCES demo_category (id)
                """);
        log.info("已补充外键 fk_demo_item_category");
    }

    private boolean columnExists(String table, String column) {
        return metadata(table, (meta, catalog, tableName) -> {
            try (ResultSet rs = meta.getColumns(catalog, null, tableName, null)) {
                while (rs.next()) {
                    if (column.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private boolean indexExists(String table, String index) {
        return metadata(table, (meta, catalog, tableName) -> {
            try (ResultSet rs = meta.getIndexInfo(catalog, null, tableName, false, false)) {
                while (rs.next()) {
                    if (index.equalsIgnoreCase(rs.getString("INDEX_NAME"))) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private boolean foreignKeyExists(String table, String constraint) {
        return metadata(table, (meta, catalog, tableName) -> {
            try (ResultSet rs = meta.getImportedKeys(catalog, null, tableName)) {
                while (rs.next()) {
                    if (constraint.equalsIgnoreCase(rs.getString("FK_NAME"))) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private boolean metadata(String table, MetaCheck check) {
        Boolean found = jdbc.execute((ConnectionCallback<Boolean>) connection -> {
            DatabaseMetaData meta = connection.getMetaData();
            String catalog = connection.getCatalog();
            for (String tableName : List.of(table, table.toUpperCase(Locale.ROOT), table.toLowerCase(Locale.ROOT))) {
                if (check.matches(meta, catalog, tableName)) {
                    return true;
                }
            }
            return false;
        });
        return Boolean.TRUE.equals(found);
    }

    @FunctionalInterface
    private interface MetaCheck {
        boolean matches(DatabaseMetaData meta, String catalog, String tableName) throws SQLException;
    }
}
