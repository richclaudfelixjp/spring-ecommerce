package com.portfolio.spring_ecommerce.util;

import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 認証されたユーザーを取得するユーティリティクラス。
 */
@Component
public class GetAuthenticatedUser {


    private final UserRepository userRepository;

    /**
     * コンストラクタでUserRepositoryを注入。
     * @param userRepository ユーザーリポジトリ
     */
    public GetAuthenticatedUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 現在認証されているユーザーを取得する。
     * @return 認証されたユーザー、存在しない場合はnull
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}
