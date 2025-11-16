package com.portfolio.spring_ecommerce.service;

import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    // Userエンティティへのデータアクセスを担当するリポジトリ
    private final UserRepository userRepository;

    // UserRepositoryをDI（依存性注入）で受け取るコンストラクタ
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザー名からユーザー情報を取得し、Spring Security用のUserDetailsとして返す
     * @param username ユーザー名
     * @return UserDetails
     * @throws UsernameNotFoundException ユーザーが見つからない場合にスロー
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ユーザー名で検索し、見つからなければ例外を投げる（日本語メッセージ）
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザー名「" + username + "」のユーザーが見つかりません。"));

        // UserエンティティからSpring SecurityのUserDetailsを生成して返す
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }
}