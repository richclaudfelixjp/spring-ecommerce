package com.portfolio.spring_ecommerce.service;

import com.portfolio.spring_ecommerce.model.Product;
import com.portfolio.spring_ecommerce.repository.ProductRepository;
import com.portfolio.spring_ecommerce.dto.ProductDto;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * 商品に関するビジネスロジックを処理するサービスクラス。
 */
@Service
public class ProductService {

    /**
     * 商品リポジトリへの参照。
     */
    private final ProductRepository productRepository;

    /**
     * ProductServiceのコンストラクタ。
     * Springの依存性注入によりProductRepositoryのインスタンスが注入される。
     * @param productRepository 商品リポジトリ
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 全ての商品を取得する。
     * @return 商品のリスト
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * IDに基づいて特定の商品を取得する。
     * @param id 取得対象の商品ID
     * @return 指定されたIDの商品。見つからない場合は空のOptionalを返す。
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 新しい商品を登録する。
     * ProductDtoからProductエンティティにデータを詰め替え、データベースに保存する。
     * 新規作成時のステータスは常にfalse（無効）に設定される。
     * @param productDto 登録する商品のデータ転送オブジェクト
     * @return 保存された商品エンティティ
     */
    public Product createProduct(ProductDto productDto) {
        Product product = new Product();

        product.setSku(productDto.getSku());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setUnitPrice(productDto.getUnitPrice());
        product.setStatus(false);
        product.setUnitsInStock(productDto.getUnitsInStock());
        
        return productRepository.save(product);
    }

    /**
     * 既存の商品情報を更新する。
     * IDで商品を検索し、nullでない項目のみを更新する。
     * @param id 更新対象の商品ID
     * @param product 更新内容を含む商品オブジェクト
     * @return 更新された商品エンティティ。商品が存在しない場合はnullを返す。
     */
    public Product updateProduct(Long id, Product product) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product existingProduct = optionalProduct.get();

            if (product.getSku() != null) {
                existingProduct.setSku(product.getSku());
            }
            if (product.getName() != null) {
                existingProduct.setName(product.getName());
            }
            if (product.getDescription() != null) {
                existingProduct.setDescription(product.getDescription());
            }
            if (product.getUnitPrice() != null) {
                existingProduct.setUnitPrice(product.getUnitPrice());
            }
            if (product.getUnitsInStock() != null) {
                existingProduct.setUnitsInStock(product.getUnitsInStock());
            }
            if (product.getStatus() != null) {
                existingProduct.setStatus(product.getStatus());
            }
            
            return productRepository.save(existingProduct);
        } else {
            return null; 
        }
    }

    /**
     * IDに基づいて商品を削除する。
     * @param id 削除対象の商品ID
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * 全ての商品を削除する。
     */
    public void deleteAllProducts() {
        productRepository.deleteAll();
    }
}