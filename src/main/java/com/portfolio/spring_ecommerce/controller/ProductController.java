package com.portfolio.spring_ecommerce.controller;

import com.portfolio.spring_ecommerce.model.Product;
import com.portfolio.spring_ecommerce.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理を処理するコントローラー
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    // ProductServiceをDI（依存性注入）で受け取るコンストラクタ
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 全商品の取得
     * @return 商品リストとHTTPステータス200
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * 商品IDによる商品取得
     * @param id 商品ID
     * @return 該当商品とHTTPステータス200、存在しない場合は404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Long id) {
        return productService.getProductById(id)
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}