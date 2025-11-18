package com.portfolio.spring_ecommerce.dto;

/**
 * ログイン成功時にJWTトークンを返すためのDTO
 */
public class AuthResponse {
    private String token;
    private String username;

    // コンストラクタ
    public AuthResponse() {}

    public AuthResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }

    // Getter/Setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}