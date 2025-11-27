package com.portfolio.spring_ecommerce.model;

import jakarta.persistence.*;

// カートアイテムエンティティ
@Entity
@Table(name = "cart_items")
public class CartItem {

    // 主キー
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 商品との多対一の関連
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // 数量
    private Integer quantity;

    // デフォルトコンストラクタ
    public CartItem() {

    }

    // コンストラクタ（商品と数量を指定）
    public CartItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // 以下、getter/setter
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Product getProduct() { 
        return product; 
    }

    public void setProduct(Product product) { 
        this.product = product; 
    }

    public Integer getQuantity() { 
        return quantity; 
    }

    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
    }
}