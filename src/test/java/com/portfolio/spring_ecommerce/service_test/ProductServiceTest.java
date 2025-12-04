package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.dto.ProductDto;
import com.portfolio.spring_ecommerce.exception.ResourceNotFoundException;
import com.portfolio.spring_ecommerce.exception.SkuAlreadyExistsException;
import com.portfolio.spring_ecommerce.model.Product;
import com.portfolio.spring_ecommerce.repository.ProductRepository;
import com.portfolio.spring_ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.Optional;

/**
 * ProductServiceの単体テストクラス。
 * Mockitoを使用してリポジトリ層をモック化し、サービス層のロジックを検証する。
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    /**
     * ProductRepositoryのモックオブジェクト。
     * データベースとの実際のやり取りをシミュレートする。
     */
    @Mock
    private ProductRepository productRepository;

    /**
     * テスト対象のProductServiceインスタンス。
     * 上記のモック（@Mock）がこのインスタンスに自動的に注入される。
     */
    @InjectMocks
    private ProductService productService;

    /**
     * createProductメソッドのテスト。
     * ProductDtoからProductエンティティが正しく生成され、保存されることを確認する。
     * 特に、ステータスがデフォルトでfalseに設定されることを検証する。
     */
    @Test
    void testCreateProduct() {
        // Arrange: テスト用のProductDtoオブジェクトを準備
        ProductDto productDto = new ProductDto();
        productDto.setSku("TEST-SKU-123");
        productDto.setName("テスト商品");
        productDto.setDescription("これはテスト商品です。");
        productDto.setUnitPrice(new BigDecimal("99.99"));
        productDto.setUnitsInStock(100);
        
        // Arrange: 保存後のProductエンティティを模倣したオブジェクトを準備
        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setSku(productDto.getSku());
        savedProduct.setName(productDto.getName());
        savedProduct.setDescription(productDto.getDescription());
        savedProduct.setUnitPrice(productDto.getUnitPrice());
        savedProduct.setStatus(false); // サービスロジックによりfalseになるはず
        savedProduct.setUnitsInStock(productDto.getUnitsInStock());

        // Arrange: repository.saveが呼ばれた際に、上記のsavedProductを返すように設定
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act: テスト対象のメソッドを呼び出し
        Product createdProduct = productService.createProduct(productDto);

        // Assert: 返されたオブジェクトがnullでないこと、各フィールドが期待通りであることを検証
        assertNotNull(createdProduct);
        assertEquals(savedProduct.getSku(), createdProduct.getSku());
        assertEquals(savedProduct.getName(), createdProduct.getName());
        assertEquals(savedProduct.getDescription(), createdProduct.getDescription());
        assertEquals(savedProduct.getUnitPrice(), createdProduct.getUnitPrice());
        assertFalse(createdProduct.getStatus()); // ステータスはfalseであるべき
        assertEquals(savedProduct.getUnitsInStock(), createdProduct.getUnitsInStock());

        // Assert: repository.saveメソッドが1回だけ呼ばれたことを確認
        verify(productRepository, times(1)).save(any(Product.class));
    }

    /**
     * updateProductメソッドの成功テスト（部分更新）。
     * 既存の商品に対して一部のフィールドのみを更新するケースを検証する。
     */
    @Test
    void testUpdateProduct_Success_PartialUpdate() {
        // Arrange: 既存の商品データと更新用データを準備
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setSku("SKU-OLD");
        existingProduct.setName("古い名前");
        existingProduct.setDescription("古い説明");
        existingProduct.setUnitPrice(new BigDecimal("10.00"));
        existingProduct.setUnitsInStock(10);
        existingProduct.setStatus(false);

        ProductDto productUpdateInfo = new ProductDto();
        productUpdateInfo.setName("新しい名前");
        productUpdateInfo.setUnitPrice(new BigDecimal("20.00"));

        // Arrange: findByIdで既存商品を返し、saveは引数をそのまま返すように設定
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: テスト対象のメソッドを呼び出し
        Product updatedProduct = productService.updateProduct(productId, productUpdateInfo);

        // Assert: 更新後のオブジェクトが期待通りであることを検証
        assertNotNull(updatedProduct);
        assertEquals(productId, updatedProduct.getId());
        assertEquals("新しい名前", updatedProduct.getName()); // 更新されたフィールド
        assertEquals(new BigDecimal("20.00"), updatedProduct.getUnitPrice()); // 更新されたフィールド
        assertEquals("SKU-OLD", updatedProduct.getSku()); // 更新されなかったフィールド
        assertEquals("古い説明", updatedProduct.getDescription()); // 更新されなかったフィールド
        assertEquals(10, updatedProduct.getUnitsInStock()); // 更新されなかったフィールド
        assertFalse(updatedProduct.getStatus()); // 更新されなかったフィールド

        // Assert: 関連メソッドが期待通りに呼ばれたことを確認
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    /**
     * updateProductメソッドの失敗テスト。
     * 更新対象の商品IDが存在しない場合にResourceNotFoundExceptionがスローされることを検証する。
     */
    @Test
    void testUpdateProduct_NotFound() {
        // Arrange: 存在しない商品IDと更新用データを準備
        Long productId = 1L;
        ProductDto productUpdateInfo = new ProductDto();
        productUpdateInfo.setName("新しい名前");

        // Arrange: findByIdが空のOptionalを返すように設定
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert: ResourceNotFoundExceptionがスローされることを確認
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productId, productUpdateInfo);
        });

        // Assert: findByIdは呼ばれるが、saveは呼ばれないことを確認
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(0)).save(any(Product.class));
    }

    /**
     * updateProductメソッドの失敗テスト。
     * 更新しようとしたSKUが既に他の商品で使われている場合にSkuAlreadyExistsExceptionがスローされることを検証する。
     */
    @Test
    void testUpdateProduct_SkuAlreadyExists() {
        // Arrange: 既存の商品データと、別の商品が使用中のSKUを持つ更新用データを準備
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setSku("SKU-OLD");
        existingProduct.setName("古い名前");
        existingProduct.setDescription("古い説明");
        existingProduct.setUnitPrice(new BigDecimal("10.00"));
        existingProduct.setUnitsInStock(10);
        existingProduct.setStatus(true);
        existingProduct.setImageURL("http://example.com/old-image.jpg");

        String newSku = "SKU-NEW-BUT-TAKEN";
        ProductDto productUpdateInfo = new ProductDto();
        productUpdateInfo.setSku(newSku);
        productUpdateInfo.setName("新しい名前");
        productUpdateInfo.setDescription("新しい説明");
        productUpdateInfo.setUnitPrice(new BigDecimal("20.00"));
        productUpdateInfo.setUnitsInStock(20);
        productUpdateInfo.setStatus(false);
        productUpdateInfo.setImageURL("http://example.com/new-image.jpg");


        // Arrange: findByIdで既存商品を返し、existsBySkuでtrueを返すように設定
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySku(newSku)).thenReturn(true);

        // Act & Assert: SkuAlreadyExistsExceptionがスローされることを確認
        assertThrows(SkuAlreadyExistsException.class, () -> {
            productService.updateProduct(productId, productUpdateInfo);
        });

        // Assert: findByIdとexistsBySkuは呼ばれるが、saveは呼ばれないことを確認
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).existsBySku(newSku);
        verify(productRepository, times(0)).save(any(Product.class));
    }
}