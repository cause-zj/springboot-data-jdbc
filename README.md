# springboot-data-jdbc

Spring Boot 示例：用 Spring Data JDBC（`spring-boot-starter-data-jdbc`）做增删改查、批量写入、批量删除、事务、分页和多表查询。本仓库不使用 SimpleDAO，不使用 JPA/Hibernate，也不使用 MyBatis-Plus。

## 版本

| 项 | 值 |
| --- | --- |
| Spring Boot | `spring-boot-starter-parent` **3.5.16** |
| Java | **21**（`java.version`） |
| 坐标 | `com.cause:springboot-data-jdbc:0.0.1-SNAPSHOT` |
| 数据访问 | `spring-boot-starter-data-jdbc`（版本由 Spring Boot 管理） |
| 数据库驱动 | `mysql-connector-j`（runtime，版本由 Spring Boot 管理） |
| 测试库 | `com.h2database:h2`（**仅 test**，不会打进运行包） |

## 配置

运行配置在 `src/main/resources/application.yml`。事务管理器是 Boot 按数据源自动配置的 `DataSourceTransactionManager`，不需要再声明。

| 配置 | 值 | 作用 |
| --- | --- | --- |
| `server.port` | `8080` | HTTP 端口 |
| `server.servlet.encoding` | UTF-8，强制覆盖 | 请求和响应编码 |
| `spring.application.name` | `springboot-data-jdbc` | 应用名，`/api/health` 会返回它 |
| `spring.datasource.*` | 见下方数据源 | MySQL 连接 |
| `spring.datasource.hikari` | 最大 10、最小空闲 2 | 连接池 |
| `spring.sql.init.mode` | `always` | 每次启动执行 `schema.sql`、`data.sql` |
| `spring.sql.init.encoding` | `UTF-8` | 脚本编码 |
| `spring.jdbc.template.query-timeout` | `30s` | 单条语句超时 |
| `spring.data.jdbc.repositories.enabled` | `true` | 启用 JDBC Repository |
| `spring.data.jdbc.dialect` | `MYSQL` | 显式使用 MySQL 方言 |
| `spring.data.web.pageable.default-page-size` | `20` | 分页默认大小 |
| `spring.data.web.pageable.max-page-size` | `200` | 分页上限 |
| `spring.data.web.pageable.one-indexed-parameters` | `false` | `page=0` 是第一页 |
| `spring.data.web.pageable.serialization-mode` | `via-dto` | 分页 JSON 使用稳定的 `content` + `page` 结构 |
| `spring.jackson.serialization.write-dates-as-timestamps` | `false` | 时间写成 ISO-8601 字符串 |
| `spring.jackson.default-property-inclusion` | `non_null` | 空字段不出现在 JSON 里 |
| `logging.level.org.springframework.jdbc.core.JdbcTemplate` | `DEBUG` | 打印 SQL |

写入时的 `createTime` / `updateTime` 使用 `Asia/Shanghai`。

## 表

| 表 | 用途 |
| --- | --- |
| `demo_category` | 分类。`id` 自增 |
| `demo_item` | 条目。`category_id` 指向分类，金额 `amount`，备注 `remark` |
| `demo_order` | 订单聚合根 |
| `demo_order_line` | 订单明细。`line_idx` 是 List 下标，和 `order_id` 组成主键，删除订单时级联删除 |
| `demo_audit` | 和订单放在同一个事务里的审计行 |

`schema.sql` 用 `CREATE TABLE IF NOT EXISTS`。如果库里还是第一版只有 `id` / `name` / `create_time` 的 `demo_item`，启动时 `DemoSchemaMigration` 会补上 `category_id`、`amount`、`remark`、`update_time`、索引和外键，并给已有种子行回填分类和金额。

`data.sql` 用 `INSERT ... WHERE NOT EXISTS`（和 `INSERT IGNORE` 一样，重复启动不会再插一行）：

| id | 数据 |
| --- | --- |
| 分类 1 | 默认分类 |
| 分类 2 | 配件 |
| 条目 1 | 示例条目，分类 1，金额 12.50 |
| 条目 2 | 备用条目，分类 2，金额 8.00 |

## 代码布局

```
com.cause
├── Application
├── controller    HealthController、DemoItemController、DemoCategoryController、DemoOrderController、ApiExceptionHandler
├── service       DemoItemService、DemoCategoryService、DemoOrderService
├── dao           Repository、联表 RowMapper、DemoSchemaMigration
└── domain        DemoItem、DemoCategory、DemoOrder、DemoOrderLine、DemoAudit、DemoItemDetail
```

Spring Data JDBC 把多表分成两种写法：

- **一对多写入**：`DemoOrder` 是聚合根，明细用 `@MappedCollection` 挂在订单上。只保存订单，就会写 `demo_order` 和 `demo_order_line`。明细没有自己的 Repository。
- **多表查询**：`DemoItemRepository.findAllWithCategory()` 用 `@Query` 做 `LEFT JOIN`，结果映射成只读的 `DemoItemDetail`。分类不是条目的子对象。

## 数据源

复用宿主机已经在跑的 MySQL，**不要**再起一个 MySQL 容器。

- Compose 目录：`~/dev-env/mysql-redis-kafka`
- 容器名：`local-mysql`
- 地址：`localhost:3306`
- 管理账号：`root` / `devroot`
- 应用账号：`app` / `app123`
- 专用库：`springboot_data_jdbc`
- 驱动：`com.mysql.cj.jdbc.Driver`
- URL：`jdbc:mysql://localhost:3306/springboot_data_jdbc?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false`

若库 `springboot_data_jdbc` 还不存在，在已有的 `local-mysql` 里建库即可，不要新建容器：

```sql
CREATE DATABASE IF NOT EXISTS springboot_data_jdbc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 运行

```bash
mvn -DskipTests package
mvn spring-boot:run
```

或直接跑打包结果：

```bash
java -jar target/springboot-data-jdbc-0.0.1-SNAPSHOT.jar
```

默认端口 `8080`。本机冒烟应连宿主机 `local-mysql`（`localhost:3306`）。下面的 `curl` 都假设种子数据已经在。

错误响应统一是 `{"error":"...","message":"..."}`。校验失败是 400，找不到是 404，外键冲突或事务演示回滚是 409。

### 健康检查

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/ping` | `{"status":"ok"}` |
| GET | `/api/health` | `{"status":"UP","application":"springboot-data-jdbc"}` |

```bash
curl -s http://localhost:8080/api/ping
curl -s http://localhost:8080/api/health
```

### 查询

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/demo-items` | 全部条目，按 id 升序 |
| GET | `/api/demo-items/{id}` | 单条 |
| GET | `/api/demo-items/page?page=0&size=1&sort=id,asc` | 分页。`name` 可选，按名称包含匹配 |
| GET | `/api/demo-items/with-category` | 条目左连接分类 |
| GET | `/api/demo-items/by-category/{categoryId}` | 某个分类下的条目 |
| GET | `/api/categories` | 全部分类 |
| GET | `/api/categories/{id}` | 单个分类 |
| GET | `/api/orders` | 全部订单（含明细） |
| GET | `/api/orders/{id}` | 单个订单（含明细） |

分页只接受这些排序字段：`id`、`name`、`amount`、`remark`、`categoryId`、`createTime`、`updateTime`。不传 `sort` 时按 `id` 升序。

```bash
curl -s http://localhost:8080/api/demo-items
curl -s http://localhost:8080/api/demo-items/1
curl -s "http://localhost:8080/api/demo-items/page?page=0&size=1&sort=id,asc"
curl -s "http://localhost:8080/api/demo-items/page?name=示例&page=0&size=10"
curl -s http://localhost:8080/api/demo-items/with-category
curl -s http://localhost:8080/api/demo-items/by-category/1
curl -s http://localhost:8080/api/categories
```

分页响应形如：

```json
{
  "content": [{"id": 1, "name": "示例条目", "categoryId": 1, "amount": 12.50}],
  "page": {"size": 1, "totalElements": 2, "totalPages": 2, "number": 0}
}
```

联表查询会多一个 `categoryName`，例如 `"categoryName":"默认分类"`。

### 新增、修改、删除

```bash
curl -s -X POST http://localhost:8080/api/categories \
  -H 'Content-Type: application/json' \
  -d '{"name":"文具"}'

curl -s -X POST http://localhost:8080/api/demo-items \
  -H 'Content-Type: application/json' \
  -d '{"name":"笔记本","categoryId":1,"amount":12.50,"remark":"A5"}'

curl -s -X PUT http://localhost:8080/api/demo-items/3 \
  -H 'Content-Type: application/json' \
  -d '{"name":"硬壳笔记本","categoryId":1,"amount":15.00,"remark":"改价"}'

curl -s -X DELETE http://localhost:8080/api/demo-items/3
```

`amount` 必填且不能为负。`categoryId` 可以不传；传了就必须是已有分类。主键不要在请求里指定，由数据库自增。删除不存在的 id 返回 404。分类下面还有条目时删除分类返回 409。

### 批量新增、批量删除

批量新增走 `ListCrudRepository.saveAll`（Spring Data JDBC 3.5 会按批插入），整个方法包在一个事务里。前面几条即使已经通过校验，只要后面有一条不合法，数据库里不会留下这批数据。单次最多 100 条。

```bash
curl -s -X POST http://localhost:8080/api/demo-items/batch \
  -H 'Content-Type: application/json' \
  -d '[
    {"name":"牛奶","categoryId":1,"amount":3.50},
    {"name":"面包","categoryId":1,"amount":6.00}
  ]'
```

批量删除是一条 `DELETE FROM demo_item WHERE id IN (...)`。重复 id 只算一次，不存在的 id 会被忽略，响应里的 `deleted` 是实际删掉的行数。单次最多 1000 个 id。

```bash
curl -s -X DELETE http://localhost:8080/api/demo-items/batch \
  -H 'Content-Type: application/json' \
  -d '{"ids":[3,4]}'
```

### 事务和一对多

`POST /api/orders` 在同一个事务里写三张表：`demo_order`、`demo_order_line`、`demo_audit`。明细按请求数组的顺序放进 `line_idx`（从 0 开始）。`totalAmount` 由服务端用单价乘数量算出来。

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "演示订单",
    "lines": [
      {"itemName": "示例条目", "quantity": 2, "price": 12.50},
      {"itemName": "第二件", "quantity": 1, "price": 3.00}
    ]
  }'
```

上面这个订单合计 `28.00`。接着可以改标题（明细还在）、再按 id 查出整单，最后删除。删除订单时会同时删掉明细和对应审计。

```bash
curl -s -X PUT http://localhost:8080/api/orders/1 \
  -H 'Content-Type: application/json' \
  -d '{"title":"改名订单"}'

curl -s http://localhost:8080/api/orders/1
curl -s -X DELETE http://localhost:8080/api/orders/1
```

把 `forceRollback` 设为 `true` 时，三张表都写入之后会抛出运行时异常，事务整体回滚，接口返回 409，库里看不到这张订单：

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "回滚订单",
    "forceRollback": true,
    "lines": [{"itemName": "示例条目", "quantity": 1, "price": 12.50}]
  }'
```

## 测试

```bash
mvn -B test
mvn -B -DskipTests package
```

`mvn test` 使用 profile `test` 和内存库 H2，不连接 MySQL。用例覆盖增删改查、批量新增失败时不留数据、批量删除、分页、分类联表、外键冲突，以及订单事务回滚。H2 只在 `test` 作用域，运行用的 jar 里没有它。

连上本机库做手工验证时，先确认 `localhost:3306` 上的 `local-mysql` 可用，再用 `mvn spring-boot:run`。
