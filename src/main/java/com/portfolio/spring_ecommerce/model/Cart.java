package com.portfolio.spring_ecommerce.model;

import jakarta.persistence.*;
import java.util.List;

// カートエンティティ
@Entity
@Table(name = "carts")
public class Cart {

    // 主キー
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ユーザーとの1対1の関連
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // カートアイテムとの1対多の関連
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private List<CartItem> items;

    // デフォルトコンストラクタ
    public Cart() {
        
    }

    // コンストラクタ（ユーザーとアイテムリストを指定）
    public Cart(User user, List<CartItem> items) {
        this.user = user;
        this.items = items;
    }

    // 以下、getter/setter
    public Long getId() { 
        return id; 
    }

    public User getUser() { 
        return user; 
    }

    public void setUser(User user) { 
        this.user = user; 
    }

    public List<CartItem> getItems() { 
        return items; 
    }
    
    public void setItems(List<CartItem> items) { 
        this.items = items; 
    }
}