package com.portfolio.spring_ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

// 商品エンティティ
@Entity
@Table(name="products")
public class Product {

    // 主キー
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // SKUコード
    @Column(name = "sku", unique = true)
    private String sku;

    // 商品名
    @Column(name = "name")
    private String name;

    // 商品説明
    @Column(name = "description")
    private String description;

    // 単価
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    // 商品ステータス（販売中かどうか）
    @Column(name = "status")
    private Boolean status;

    // 在庫数
    @Column(name = "units_in_stock")
    private Integer unitsInStock;

    // 画像URL
    @Column(name = "image_url")
    private String imageURL;

    // 以下、getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
 
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getUnitsInStock() {
        return unitsInStock;
    }

    public void setUnitsInStock(Integer unitsInStock) {
        this.unitsInStock = unitsInStock;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}