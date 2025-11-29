package com.portfolio.spring_ecommerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * テスト用のコントローラー
 */
@RestController
public class TestController {
    
    /**
     * 接続テスト用エンドポイント
     */
    @GetMapping("/test")
    public String test() {
        return "接続成功";
    }
}