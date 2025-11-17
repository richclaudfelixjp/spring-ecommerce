package com.portfolio.spring_ecommerce.service;

import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.stream.Collectors;

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
        // ユーザー名でDBからユーザーを検索。見つからなければ例外をスロー
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザー名「" + username + "」のユーザーが見つかりません。"));

        // ユーザーのロールをSpring Securityの権限リストに変換
        var authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // UserDetails（Spring Security標準のユーザー情報）を生成して返却
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}