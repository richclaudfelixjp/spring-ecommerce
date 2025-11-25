package com.portfolio.spring_ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * 商品情報を表すエンティティクラス。
 */
@Entity
@Table(name="products")
public class Product {

    /**
     * 商品ID。データベースによって自動採番される主キー。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * SKU (Stock Keeping Unit)。在庫管理上の最小単位を識別するコード。
     */
    @Column(name = "sku")
    private String sku;

    /**
     * 商品名。
     */
    @Column(name = "name")
    private String name;

    /**
     * 商品の詳細な説明。
     */
    @Column(name = "description")
    private String description;

    /**
     * 商品の単価。
     */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /**
     * 商品のステータス（例: true=有効, false=無効）。
     */
    @Column(name = "status")
    private Boolean status;

    /**
     * 現在の在庫数。
     */
    @Column(name = "units_in_stock")
    private Integer unitsInStock;

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
}