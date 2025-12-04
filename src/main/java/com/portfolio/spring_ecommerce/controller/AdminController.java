package com.portfolio.spring_ecommerce.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.portfolio.spring_ecommerce.dto.ProductDto;
import com.portfolio.spring_ecommerce.model.Product;
import com.portfolio.spring_ecommerce.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 管理者用のコントローラー
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;

    // ProductServiceをDI（依存性注入）で受け取るコンストラクタ
    public AdminController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 管理者用ダッシュボード画面
     * アクセスにはADMINロールが必要
     * @return 管理者ダッシュボードのメッセージ
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')") // ADMINロールのみアクセス可能
    public String adminDashboard() {
        return "管理者ダッシュボード - アクセス成功";
    }

    /**
     * ユーザー管理画面
     * アクセスにはADMINロールが必要
     * @return ユーザー管理画面のメッセージ
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // ADMINロールのみアクセス可能
    public String manageUsers() {
        return "ユーザー管理画面";
    }

    /**
     * 商品の新規作成
     * @param productDto 商品情報DTO（バリデーション付き）
     * @return 作成された商品情報とHTTPステータス201
     */
    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')") // ADMINロールのみアクセス可能
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDto productDto) {
        Product newProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    /**
     * 商品の更新（ID指定）
     * @param id 商品ID
     * @param productDto 更新内容を含む商品情報DTO（バリデーション付き）
     * @return 更新された商品情報とHTTPステータス200
     */
    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')") // ADMINロールのみアクセス可能
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Long id, @Valid @RequestBody ProductDto productDto) {
        Product updatedProduct = productService.updateProduct(id, productDto);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    /**
     * 商品の削除（ID指定）
     * @param id 商品ID
     * @return 削除成功時はHTTPステータス204
     */
    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')") // ADMINロールのみアクセス可能
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 全商品の削除
     * @return 削除成功時はHTTPステータス204
     */
    @DeleteMapping("/products")
    @PreAuthorize("hasRole('ADMIN')") // ADMINロールのみアクセス可能
    public ResponseEntity<Void> deleteAllProducts() {
        productService.deleteAllProducts();
        return ResponseEntity.noContent().build();
    }
}