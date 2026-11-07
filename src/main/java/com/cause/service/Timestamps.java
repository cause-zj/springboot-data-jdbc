package com.cause.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

final class Timestamps {

    static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private Timestamps() {
    }

    static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }
}
