package com.portfolio.spring_ecommerce.repository;

import com.portfolio.spring_ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
}