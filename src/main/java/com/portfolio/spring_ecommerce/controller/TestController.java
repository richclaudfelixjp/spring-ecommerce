package com.portfolio.spring_ecommerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// テスト用のコントローラー
@RestController
public class TestController {
    
    // /test エンドポイントにアクセスした際に「接続成功」と返す
    @GetMapping("/test")
    public String test() {
        return "接続成功";
    }
}