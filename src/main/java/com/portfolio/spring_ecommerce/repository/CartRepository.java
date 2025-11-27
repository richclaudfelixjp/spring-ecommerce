package com.portfolio.spring_ecommerce.repository;

import com.portfolio.spring_ecommerce.model.Cart;
import com.portfolio.spring_ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}