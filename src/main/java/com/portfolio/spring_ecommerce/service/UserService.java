package com.portfolio.spring_ecommerce.service;

import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.stream.Collectors;

/**
 * ユーザー認証およびユーザー情報取得を担当するサービスクラス。
 * Spring SecurityのUserDetailsServiceを実装し、ユーザー名による認証処理を提供する。
 */
@Service
public class UserService implements UserDetailsService {

    /**
     * ユーザーリポジトリへの参照。
     */
    private final UserRepository userRepository;

    /**
     * UserServiceのコンストラクタ。
     * Springの依存性注入によりUserRepositoryのインスタンスが注入される。
     * @param userRepository ユーザーリポジトリ
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザー名に基づいてユーザー情報を取得し、Spring Security用のUserDetailsを返す。
     * ユーザーが存在しない場合は例外をスローする。
     * @param username 認証対象のユーザー名
     * @return UserDetails（Spring Security用ユーザー情報）
     * @throws UsernameNotFoundException ユーザーが見つからない場合にスローされる例外
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザー名「" + username + "」のユーザーが見つかりません。"));

        var authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}