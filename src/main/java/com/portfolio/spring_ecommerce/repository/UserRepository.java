package com.portfolio.spring_ecommerce.repository;

import com.portfolio.spring_ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Userエンティティに対するDB操作を行うリポジトリインターフェース
public interface UserRepository extends JpaRepository<User, Long> {
    // ユーザー名でユーザーを検索するメソッド
    Optional<User> findByUsername(String username);
}