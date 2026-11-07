CREATE TABLE IF NOT EXISTS demo_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    create_time TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS demo_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT NULL,
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    remark VARCHAR(200) NULL,
    create_time TIMESTAMP NULL,
    update_time TIMESTAMP NULL,
    CONSTRAINT fk_demo_item_category FOREIGN KEY (category_id) REFERENCES demo_category (id)
);

CREATE INDEX IF NOT EXISTS idx_demo_item_category ON demo_item (category_id);

CREATE TABLE IF NOT EXISTS demo_order (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    create_time TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS demo_order_line (
    order_id BIGINT NOT NULL,
    line_idx INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (order_id, line_idx),
    CONSTRAINT fk_demo_order_line_order FOREIGN KEY (order_id) REFERENCES demo_order (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demo_audit (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    biz_type VARCHAR(32) NOT NULL,
    biz_id BIGINT NOT NULL,
    message VARCHAR(200) NOT NULL,
    create_time TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_demo_audit_biz ON demo_audit (biz_type, biz_id);
