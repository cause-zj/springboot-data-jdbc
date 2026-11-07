package com.cause.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 演示条目，对应表 {@code demo_item}。
 * 主键 {@code id} 由 MySQL {@code AUTO_INCREMENT} 生成；{@code createTime} 映射列 {@code create_time}。
 */
@Table("demo_item")
public class DemoItem {

    @Id
    private Long id;

    private String name;

    @Column("create_time")
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
