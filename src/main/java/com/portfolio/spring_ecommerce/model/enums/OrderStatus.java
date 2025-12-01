package com.portfolio.spring_ecommerce.model.enums;

// 注文ステータスの列挙型
public enum OrderStatus {
    // 保留中
    PENDING,
    // 支払い済み
    PAID,
    // キャンセル済み
    CANCELLED
}