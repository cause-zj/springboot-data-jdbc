package com.cause.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单聚合根。明细通过 {@link MappedCollection} 落到 {@code demo_order_line}，
 * {@code line_idx} 保存 List 下标，不单独建仓库。
 */
@Table("demo_order")
public class DemoOrder {

    @Id
    private Long id;

    private String title;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("create_time")
    private LocalDateTime createTime;

    @MappedCollection(idColumn = "order_id", keyColumn = "line_idx")
    private List<DemoOrderLine> lines = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<DemoOrderLine> getLines() {
        return lines;
    }

    public void setLines(List<DemoOrderLine> lines) {
        this.lines = lines == null ? new ArrayList<>() : lines;
    }
}
