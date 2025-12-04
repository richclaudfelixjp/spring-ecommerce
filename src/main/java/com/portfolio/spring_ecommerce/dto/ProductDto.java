package com.portfolio.spring_ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * 商品データの転送オブジェクト（DTO）。
 * 主に商品作成・更新時のリクエストボディとして使用される。
 */
public class ProductDto {

    /**
     * 商品のSKU（Stock Keeping Unit）。必須項目。
     */
    @NotBlank(message = "SKUは必須です")
    private String sku;

    /**
     * 商品名。必須項目。
     */
    @NotBlank(message = "商品名は必須です")
    private String name;

    /**
     * 商品の説明。
     */
    private String description;

    /**
     * 商品の単価。
     */
    private BigDecimal unitPrice;

    /**
     * 商品のステータス（例：有効か無効か）。
     */
    private Boolean status;

    /**
     * 在庫数。
     */
    private Integer unitsInStock;

    /**
     * 商品画像のURL。
     */
    private String imageURL;

    // 以下、各フィールドのgetter/setter

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