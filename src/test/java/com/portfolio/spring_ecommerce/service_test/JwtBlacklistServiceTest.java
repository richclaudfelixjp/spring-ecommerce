package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.service.JwtBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtBlacklistServiceの単体テストクラス。
 * JWTトークンのブラックリスト管理ロジックを検証する。
 */
class JwtBlacklistServiceTest {

    /**
     * テスト対象のJwtBlacklistServiceインスタンス。
     * 各テスト実行前に初期化される。
     */
    private JwtBlacklistService jwtBlacklistService;

    /**
     * 各テスト実行前にJwtBlacklistServiceを初期化する。
     */
    @BeforeEach
    void setUp() {
        jwtBlacklistService = new JwtBlacklistService();
    }

    /**
     * addTokenToBlacklistメソッドとisTokenBlacklistedメソッドの連携テスト。
     * トークンがブラックリストに追加された後、正しく判定されることを検証する。
     */
    @Test
    void whenTokenAdded_shouldBeBlacklisted() {
        String token = "test-token";
        
        // ブラックリスト追加前はfalse
        assertFalse(jwtBlacklistService.isTokenBlacklisted(token));

        // トークンをブラックリストに追加
        jwtBlacklistService.addTokenToBlacklist(token);

        // ブラックリスト追加後はtrue
        assertTrue(jwtBlacklistService.isTokenBlacklisted(token));
    }
}