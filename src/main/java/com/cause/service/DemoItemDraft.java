package com.cause.service;

import java.math.BigDecimal;

public record DemoItemDraft(String name, Long categoryId, BigDecimal amount, String remark) {
}
