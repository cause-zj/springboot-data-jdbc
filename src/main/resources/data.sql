INSERT INTO demo_category (id, name, create_time)
SELECT 1, '默认分类', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM demo_category WHERE id = 1);

INSERT INTO demo_category (id, name, create_time)
SELECT 2, '配件', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM demo_category WHERE id = 2);

INSERT INTO demo_item (id, name, create_time)
SELECT 1, '示例条目', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM demo_item WHERE id = 1);

INSERT INTO demo_item (id, name, create_time)
SELECT 2, '备用条目', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM demo_item WHERE id = 2);
