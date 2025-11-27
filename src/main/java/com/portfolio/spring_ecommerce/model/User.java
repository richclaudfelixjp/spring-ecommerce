package com.portfolio.spring_ecommerce.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Set;

// ユーザーエンティティ
@Entity
@Table(name = "app_users")
public class User {

    // 主キー
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ユーザー名
    private String username;

    // パスワード
    private String password;

    // ロール（権限）のセット
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;

    // コンストラクタ（ユーザー名とパスワードを指定）
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // デフォルトコンストラクタ
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