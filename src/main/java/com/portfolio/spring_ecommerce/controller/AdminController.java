package com.portfolio.spring_ecommerce.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // このクラスがREST APIのコントローラーであることを示す
@RequestMapping("/admin") // /adminパス以下のリクエストをこのコントローラーで処理
public class AdminController {

    /**
     * 管理者用ダッシュボード画面
     * アクセスにはADMINロールが必要
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')") // ADMINロールのみアクセス可能
    public String adminDashboard() {
        return "管理者ダッシュボード - アクセス成功";
    }

    /**
     * ユーザー管理画面
     * アクセスにはADMINロールが必要
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // ADMINロールのみアクセス可能
    public String manageUsers() {
        return "ユーザー管理画面";
    }
}