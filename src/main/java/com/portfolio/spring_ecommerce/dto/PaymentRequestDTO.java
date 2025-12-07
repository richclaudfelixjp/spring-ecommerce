package com.portfolio.spring_ecommerce.dto;

/**
 * 支払いリクエスト情報を表すDTO
 */
public class PaymentRequestDTO {
    private Long orderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}