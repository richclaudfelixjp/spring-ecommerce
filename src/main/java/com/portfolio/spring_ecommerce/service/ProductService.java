package com.portfolio.spring_ecommerce.service;

import com.portfolio.spring_ecommerce.model.Product;
import com.portfolio.spring_ecommerce.repository.ProductRepository;
import com.portfolio.spring_ecommerce.dto.ProductDto;
import com.portfolio.spring_ecommerce.exception.ResourceNotFoundException;
import com.portfolio.spring_ecommerce.exception.SkuAlreadyExistsException;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
     * 新しい商品を作成する。
     * @param productDto 作成する商品のデータを含むDTO
     * @return 作成された商品エンティティ
     */
    public Product createProduct(ProductDto productDto) {
        if (productRepository.existsBySku(productDto.getSku())) {
            throw new SkuAlreadyExistsException("SKUが既に存在します: " + productDto.getSku());
        }

        Product product = new Product();

        product.setSku(productDto.getSku());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());

        if(productDto.getUnitPrice() == null) {
            product.setUnitPrice(BigDecimal.ZERO);
        } else {
            product.setUnitPrice(productDto.getUnitPrice());
        }

        if(productDto.getStatus() == null) {
            product.setStatus(false);
        } else {
            product.setStatus(true);
        }

        if(productDto.getUnitsInStock() == null) {
            product.setUnitsInStock(0);
        } else {
            product.setUnitsInStock(productDto.getUnitsInStock());
        }
                
        return productRepository.save(product);
    }

    /**
     * 既存の商品情報を更新する。
     * IDで商品を検索し、nullでない項目のみを更新する。
     * @param id 更新対象の商品ID
     * @param product 更新内容を含む商品オブジェクト
     * @return 更新された商品エンティティ。商品が存在しない場合はnullを返す。
     */
    public Product updateProduct(Long id, ProductDto productDto) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product existingProduct = optionalProduct.get();

            if (productDto.getSku() != null && !productDto.getSku().equals(existingProduct.getSku())) {
                if (productRepository.existsBySku(productDto.getSku())) {
                    throw new SkuAlreadyExistsException("SKUが既に存在します: " + productDto.getSku());
                }
                existingProduct.setSku(productDto.getSku());
            }
            if (productDto.getName() != null) {
                existingProduct.setName(productDto.getName());
            }
            if (productDto.getDescription() != null) {
                existingProduct.setDescription(productDto.getDescription());
            }
            if (productDto.getUnitPrice() != null) {
                existingProduct.setUnitPrice(productDto.getUnitPrice());
            }
            if (productDto.getUnitsInStock() != null) {
                existingProduct.setUnitsInStock(productDto.getUnitsInStock());
            }
            if (productDto.getStatus() != null) {
                existingProduct.setStatus(productDto.getStatus());
            }
            if (productDto.getImageURL() != null) {
                existingProduct.setImageURL(productDto.getImageURL());
            }
            
            return productRepository.save(existingProduct);
        } else {
            throw new ResourceNotFoundException("商品が見つかりません。");
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

    /**
     * 商品の画像URLを更新する。
     * @param id 更新対象の商品ID
     * @param imageUrl 更新後の画像URL
     * @return 更新された商品エンティティ
     */
    public Product updateProductImage(Long id, String imageUrl) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品が見つかりません。 ID: " + id));
        product.setImageURL(imageUrl);
        return productRepository.save(product);
    }
}