package com.portfolio.spring_ecommerce.dto;

import java.util.List;

public class UserOrdersResponseDTO {

    private List<OrderDTO> orders;
    private String username;

    public UserOrdersResponseDTO(String username, List<OrderDTO> orders) {
        this.orders = orders;
        this.username = username;
    }

    public List<OrderDTO> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDTO> orders) {
        this.orders = orders;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}