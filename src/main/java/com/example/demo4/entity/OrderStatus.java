package com.example.demo4.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    COMPLETED,
    CANCELLED;

    @JsonCreator
    public static OrderStatus from(String value) {
        return OrderStatus.valueOf(value.toUpperCase());
    }
}
