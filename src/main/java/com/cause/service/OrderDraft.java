package com.cause.service;

import java.math.BigDecimal;
import java.util.List;

public record OrderDraft(String title, List<Line> lines, Boolean forceRollback) {

    public record Line(String itemName, Integer quantity, BigDecimal price) {
    }

    public boolean rollbackRequested() {
        return Boolean.TRUE.equals(forceRollback);
    }
}
