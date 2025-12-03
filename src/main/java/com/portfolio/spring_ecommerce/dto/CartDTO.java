package com.portfolio.spring_ecommerce.dto;

import com.portfolio.spring_ecommerce.model.CartItem;

import java.util.List;

/**
 * カートのデータDTO（Data Transfer Object）
 */
public class CartDTO {
    private String username;
    private List<CartItem> items;
    private double totalPrice;

    public CartDTO(String username, List<CartItem> items) {
        this.username = username;
        this.items = items;
        this.totalPrice = calculateTotalPrice(items);
    }

    // カート内商品の合計金額を計算するヘルパーメソッド
    private double calculateTotalPrice(List<CartItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getProduct().getUnitPrice().doubleValue() * item.getQuantity())
                .sum();
    }

    // 以下、各フィールドのgetter/setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}