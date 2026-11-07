# springboot-data-jdbc

最小 Spring Boot 示例：用 Spring Data JDBC（`spring-boot-starter-data-jdbc`）做单表访问。本仓库不使用 SimpleDAO，不使用 JPA/Hibernate，也不使用 MyBatis-Plus。

## 版本

| 项 | 值 |
| --- | --- |
| Spring Boot | `spring-boot-starter-parent` **3.5.16** |
| Java | **21**（`java.version`） |
| 坐标 | `com.cause:springboot-data-jdbc:0.0.1-SNAPSHOT` |
| 数据访问 | `spring-boot-starter-data-jdbc`（版本由 Spring Boot 管理） |
| 数据库驱动 | `mysql-connector-j`（runtime，版本由 Spring Boot 管理） |

## 代码布局

```
com.cause
├── Application
├── controller    HealthController、DemoItemController
├── service       DemoItemService
├── dao           DemoItemRepository（ListCrudRepository）
└── domain        DemoItem（Spring Data JDBC @Table / @Id）
```

`DemoItem` 对应表 `demo_item`。主键 `id` 为 `BIGINT AUTO_INCREMENT`（`org.springframework.data.annotation.Id`）；业务字段 `name`；`createTime` 用 `@Column("create_time")` 映射。`DemoItemRepository` 继承 `ListCrudRepository<DemoItem, Long>`，列表接口返回表中全部行。

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

`spring.sql.init.mode` 为 `always`，脚本编码 UTF-8。启动时执行 `schema.sql`（`CREATE TABLE IF NOT EXISTS demo_item`，主键自增）和 `data.sql`（`INSERT IGNORE` 插入一条「示例条目」，重复启动不会再插一行）。新克隆的环境也能把表建出来；表已存在时不会重复建表，种子数据也不会重复插入。

若库 `springboot_data_jdbc` 还不存在，在已有的 `local-mysql` 里建库即可，不要新建容器。

## 运行

```bash
mvn -DskipTests package
mvn spring-boot:run
```

或直接跑打包结果：

```bash
java -jar target/springboot-data-jdbc-0.0.1-SNAPSHOT.jar
```

默认端口 `8080`。本机冒烟应连宿主机 `local-mysql`（`localhost:3306`）。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/ping` | `{"status":"ok"}` |
| GET | `/api/health` | `{"status":"UP","application":"springboot-data-jdbc"}` |
| GET | `/api/demo-items` | 通过 Spring Data JDBC 列出 `demo_item`；库里还没有数据时返回 `[]` |

```bash
curl -s http://localhost:8080/api/ping
curl -s http://localhost:8080/api/health
curl -s http://localhost:8080/api/demo-items
```

## 测试

```bash
mvn -B test
```

接口测试使用 `@WebMvcTest`，不连接 MySQL。连上本机库做一次手工验证时，先确认 `localhost:3306` 上的 `local-mysql` 可用，再用上面的 `mvn spring-boot:run`。
