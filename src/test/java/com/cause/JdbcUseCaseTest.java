package com.cause;

import com.cause.dao.DemoSchemaMigration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JdbcUseCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DemoSchemaMigration migration;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM demo_order_line");
        jdbc.update("DELETE FROM demo_audit");
        jdbc.update("DELETE FROM demo_order");
        jdbc.update("DELETE FROM demo_item");
        jdbc.update("DELETE FROM demo_category");
    }

    @Test
    void crudUpdateAndDeleteItem() throws Exception {
        long categoryId = createCategory("文具");
        MvcResult created = mockMvc.perform(post("/api/demo-items")
                        .contentType("application/json")
                        .content("""
                                {"name":"笔记本","categoryId":%d,"amount":12.5,"remark":"  A5  "}
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("笔记本"))
                .andExpect(jsonPath("$.amount").value(12.5))
                .andExpect(jsonPath("$.remark").value("A5"))
                .andExpect(jsonPath("$.createTime").exists())
                .andReturn();
        long id = idOf(created);
        String createTime = objectMapper.readTree(body(created)).get("createTime").asText();

        mockMvc.perform(get("/api/demo-items/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("笔记本"));

        mockMvc.perform(put("/api/demo-items/" + id)
                        .contentType("application/json")
                        .content("""
                                {"name":"硬壳笔记本","categoryId":%d,"amount":15,"remark":"改价"}
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("硬壳笔记本"))
                .andExpect(jsonPath("$.amount").value(15))
                .andExpect(jsonPath("$.createTime").value(createTime))
                .andExpect(jsonPath("$.updateTime").exists());

        mockMvc.perform(delete("/api/demo-items/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/demo-items/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void batchCreateRollsBackWhenALaterRowIsInvalid() throws Exception {
        long categoryId = createCategory("食品");
        mockMvc.perform(post("/api/demo-items/batch")
                        .contentType("application/json")
                        .content("""
                                [
                                  {"name":"牛奶","categoryId":%d,"amount":3.5},
                                  {"name":"不存在的分类","categoryId":99999,"amount":1}
                                ]
                                """.formatted(categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("分类不存在: 99999"));
        assertThat(count("demo_item")).isZero();

        mockMvc.perform(post("/api/demo-items/batch")
                        .contentType("application/json")
                        .content("""
                                [
                                  {"name":"牛奶","categoryId":%d,"amount":3.5},
                                  {"name":"面包","categoryId":%d,"amount":6},
                                  {"name":"鸡蛋","categoryId":%d,"amount":8.25}
                                ]
                                """.formatted(categoryId, categoryId, categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3));
        assertThat(count("demo_item")).isEqualTo(3);
    }

    @Test
    void batchDeleteRemovesOnlyRequestedIds() throws Exception {
        long categoryId = createCategory("配件");
        long first = createItem(categoryId, "螺丝");
        long second = createItem(categoryId, "螺母");
        long third = createItem(categoryId, "垫片");

        mockMvc.perform(delete("/api/demo-items/batch")
                        .contentType("application/json")
                        .content("{\"ids\":[%d,%d,%d]}".formatted(first, second, second)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(2));

        assertThat(count("demo_item")).isEqualTo(1);
        mockMvc.perform(get("/api/demo-items/" + third))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("垫片"));
    }

    @Test
    void pageAndNameFilter() throws Exception {
        long categoryId = createCategory("水果");
        createItem(categoryId, "苹果");
        createItem(categoryId, "苹果汁");
        createItem(categoryId, "香蕉");
        createItem(categoryId, "葡萄");
        createItem(categoryId, "西瓜");

        mockMvc.perform(get("/api/demo-items/page")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(3))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.content[0].name").value("苹果"));

        mockMvc.perform(get("/api/demo-items/page")
                        .param("name", "苹果")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2));

        mockMvc.perform(get("/api/demo-items/page").param("sort", "missing,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("排序字段不存在"));
    }

    @Test
    void joinCategoryAndRejectDeleteInUse() throws Exception {
        long categoryId = createCategory("水果");
        createItem(categoryId, "苹果");

        mockMvc.perform(get("/api/demo-items/with-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("苹果"))
                .andExpect(jsonPath("$[0].categoryName").value("水果"))
                .andExpect(jsonPath("$[0].categoryId").value(categoryId));

        mockMvc.perform(get("/api/demo-items/by-category/" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("conflict"));
        assertThat(count("demo_category")).isEqualTo(1);
    }

    @Test
    void orderWriteIsTransactionalAndRollbackRemovesEveryTable() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "回滚订单",
                                  "forceRollback": true,
                                  "lines": [
                                    {"itemName": "示例条目", "quantity": 2, "price": 12.50},
                                    {"itemName": "第二件", "quantity": 1, "price": 3}
                                  ]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("rolled_back"));
        assertThat(count("demo_order")).isZero();
        assertThat(count("demo_order_line")).isZero();
        assertThat(count("demo_audit")).isZero();

        MvcResult placed = mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "演示订单",
                                  "lines": [
                                    {"itemName": "示例条目", "quantity": 2, "price": 12.50},
                                    {"itemName": "第二件", "quantity": 1, "price": 3}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(28.0))
                .andExpect(jsonPath("$.lines.length()").value(2))
                .andReturn();
        long orderId = idOf(placed);

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("演示订单"))
                .andExpect(jsonPath("$.lines[0].itemName").value("示例条目"))
                .andExpect(jsonPath("$.lines[1].quantity").value(1));

        BigDecimal total = jdbc.queryForObject(
                "SELECT total_amount FROM demo_order WHERE id = ?", BigDecimal.class, orderId);
        assertThat(total).isEqualByComparingTo("28.00");
        assertThat(jdbc.queryForList(
                "SELECT line_idx FROM demo_order_line WHERE order_id = ? ORDER BY line_idx", Integer.class, orderId))
                .containsExactly(0, 1);
        assertThat(count("demo_audit")).isEqualTo(1);

        mockMvc.perform(put("/api/orders/" + orderId)
                        .contentType("application/json")
                        .content("{\"title\":\"改名订单\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("改名订单"))
                .andExpect(jsonPath("$.lines.length()").value(2));
        assertThat(count("demo_order_line")).isEqualTo(2);

        mockMvc.perform(delete("/api/orders/" + orderId))
                .andExpect(status().isNoContent());
        assertThat(count("demo_order")).isZero();
        assertThat(count("demo_order_line")).isZero();
        assertThat(count("demo_audit")).isZero();
    }

    @Test
    void migrationAddsDroppedColumnAndCanRunTwice() {
        jdbc.execute("ALTER TABLE demo_item DROP COLUMN remark");
        try {
            migration.run((ApplicationArguments) null);
            assertThat(columnCount("remark")).isEqualTo(1);
            migration.run((ApplicationArguments) null);
            assertThat(columnCount("remark")).isEqualTo(1);
        } finally {
            if (columnCount("remark") == 0) {
                jdbc.execute("ALTER TABLE demo_item ADD COLUMN remark VARCHAR(200)");
            }
        }
    }

    private long createCategory(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    private long createItem(long categoryId, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/demo-items")
                        .contentType("application/json")
                        .content("""
                                {"name":"%s","categoryId":%d,"amount":1.00}
                                """.formatted(name, categoryId)))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    private long idOf(MvcResult result) throws Exception {
        return objectMapper.readTree(body(result)).get("id").asLong();
    }

    private static String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private int count(String table) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        return count == null ? 0 : count;
    }

    private int columnCount(String column) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                 WHERE LOWER(table_name) = 'demo_item'
                   AND LOWER(column_name) = ?
                """, Integer.class, column);
        return count == null ? 0 : count;
    }
}
