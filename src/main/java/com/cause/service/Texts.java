package com.cause.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class Texts {

    private Texts() {
    }

    static String required(String value, String label, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new IllegalArgumentException(label + "最长 " + max + " 个字符");
        }
        return trimmed;
    }

    static String optional(String value, String label, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new IllegalArgumentException(label + "最长 " + max + " 个字符");
        }
        return trimmed;
    }

    static BigDecimal money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("金额不能为负");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
