package com.portfolio.spring_ecommerce.controller_test;

import com.portfolio.spring_ecommerce.model.Product;
import com.portfolio.spring_ecommerce.service.ProductService;
import com.portfolio.spring_ecommerce.controller.ProductController;
import com.portfolio.spring_ecommerce.filter.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProductControllerの結合テストクラス
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false) // MockMvcを自動設定

class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc; // コントローラーのテスト用MockMvc

    @MockitoBean
    private ProductService productService; // ProductServiceのモック

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; // JWT認証フィルターのモック

    @Test
    @WithMockUser // 認証されたユーザーとしてテストを実行
    void getAllProducts_shouldReturnProductListAndStatus200() throws Exception {
        // テスト用のProductデータを作成
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product A");
        product1.setDescription("Desc A");
        
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product B");
        product2.setDescription("Desc B");

        // ProductServiceのgetAllProductsメソッドの戻り値をモック
        Mockito.when(productService.getAllProducts()).thenReturn(Arrays.asList(product1, product2));

        // /productsエンドポイントのGETリクエストをテスト
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk()) // ステータス200を期待
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // JSONレスポンスを期待
                .andExpect(jsonPath("$.length()").value(2)) // 配列の長さが2
                .andExpect(jsonPath("$[0].id").value(product1.getId())) // 1件目のID
                .andExpect(jsonPath("$[0].name").value(product1.getName())) // 1件目の名前
                .andExpect(jsonPath("$[1].id").value(product2.getId())); // 2件目のID
    }

    @Test
    void getProductById_whenExists_shouldReturnProductAndStatus200() throws Exception {
        // テスト用のProductデータを作成
        Product product = new Product();
        product.setId(1L);
        product.setName("Product A");
        product.setDescription("Desc A");

        // ProductServiceのgetProductByIdメソッドの戻り値をモック
        Mockito.when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        // /products/1エンドポイントのGETリクエストをテスト
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk()) // ステータス200を期待
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // JSONレスポンスを期待
                .andExpect(jsonPath("$.id").value(product.getId())) // IDが一致
                .andExpect(jsonPath("$.name").value(product.getName())); // 名前が一致
    }

    @Test
    void getProductById_whenNotExists_shouldReturnStatus404() throws Exception {
        // ProductServiceのgetProductByIdメソッドの戻り値をモック（存在しない場合）
        Mockito.when(productService.getProductById(anyLong())).thenReturn(Optional.empty());

        // /products/999エンドポイントのGETリクエストをテスト
        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound()); // ステータス404を期待
    }
}