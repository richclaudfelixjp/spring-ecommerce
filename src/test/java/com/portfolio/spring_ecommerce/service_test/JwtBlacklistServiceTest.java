package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.service.JwtBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtBlacklistServiceTest {

    private JwtBlacklistService jwtBlacklistService;

    /**
     * 各テストの前に新しいJwtBlacklistServiceインスタンスを作成する
     */
    @BeforeEach
    void setUp() {
        jwtBlacklistService = new JwtBlacklistService();
    }

    /**
     * トークンをブラックリストに追加した場合、ブラックリストに含まれることを確認するテスト
     */
    @Test
    void whenTokenAdded_shouldBeBlacklisted() {
        String token = "test-token";
        // 初期状態ではトークンがブラックリストに含まれていないことを確認
        assertFalse(jwtBlacklistService.isTokenBlacklisted(token));

        // トークンをブラックリストに追加
        jwtBlacklistService.addTokenToBlacklist(token);

        // トークンがブラックリストに含まれていることを確認
        assertTrue(jwtBlacklistService.isTokenBlacklisted(token));
    }
}