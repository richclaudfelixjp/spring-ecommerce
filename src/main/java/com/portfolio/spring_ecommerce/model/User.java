package com.portfolio.spring_ecommerce.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "app_users") // DB上のテーブル名を指定
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主キーの自動生成
    private Long id;
    private String username;
    private String password;

    @ElementCollection(fetch = FetchType.EAGER) // 役割（ロール）をEAGERで取得
    private Set<String> roles;

    // ユーザー名とパスワードを受け取るコンストラクタ
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // デフォルトコンストラクタ（JPA用）
    public User() {}

    // 以下、getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}