package com.portfolio.spring_ecommerce.repository;

import com.portfolio.spring_ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品エンティティのデータアクセスを行うリポジトリインターフェース。
 * Spring Data JPA を利用して、Productエンティティに対する基本的なCRUD操作を提供する。
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
}