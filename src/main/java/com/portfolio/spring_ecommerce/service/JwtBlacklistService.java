package com.portfolio.spring_ecommerce.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtBlacklistService {

    private final Set<String> tokenBlacklist = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * トークンをブラックリストに追加する
     * @param token ブラックリストに追加するJWT
     */
    public void addTokenToBlacklist(String token) {
        tokenBlacklist.add(token);
    }

    /**
     * トークンがブラックリストに含まれているか確認する
     * @param token チェックするJWT
     * @return トークンがブラックリストに含まれていればtrue、そうでなければfalse
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }
}