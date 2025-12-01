package com.portfolio.spring_ecommerce.dto;

import com.portfolio.spring_ecommerce.model.OrderItem;

/**
 * 注文アイテム情報を表すDTO
 */
public class OrderItemDTO {

    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;

    // コンストラクタ
    public OrderItemDTO(OrderItem orderItem) {
        this.productId = orderItem.getProduct().getId();
        this.productName = orderItem.getProduct().getName();
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getPrice();
    }

    // 以下、各フィールドのgetter/setter

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}