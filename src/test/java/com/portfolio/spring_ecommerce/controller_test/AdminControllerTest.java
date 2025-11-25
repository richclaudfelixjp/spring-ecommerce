package com.portfolio.spring_ecommerce.controller_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.spring_ecommerce.controller.AdminController;
import com.portfolio.spring_ecommerce.dto.ProductDto;
import com.portfolio.spring_ecommerce.model.Product;
import com.portfolio.spring_ecommerce.util.JwtUtil;
import com.portfolio.spring_ecommerce.service.JwtBlacklistService;
import com.portfolio.spring_ecommerce.service.ProductService;
import com.portfolio.spring_ecommerce.service.UserService;
import com.portfolio.spring_ecommerce.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

/**
 * AdminControllerの結合テストクラス
 */
@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class) // セキュリティ設定をインポートしてテストに適用
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc; // モックMVCを使用してコントローラのテストを実施

    @Autowired
    private ObjectMapper objectMapper; // JSONのシリアライズ/デシリアライズに使用

    @MockitoBean
    private JwtUtil jwtUtil; // JwtUtilのモック

    @MockitoBean
    private UserService userService; // UserServiceのモック

    @MockitoBean
    private JwtBlacklistService jwtBlacklistService; // JwtBlacklistServiceのモック

    @MockitoBean
    private ProductService productService; // ProductServiceのモック

    @MockitoBean
    private SecurityConfig securityConfig; // SecurityConfigのモック

    /**
     * 管理者ダッシュボードへのアクセスが成功することを検証
     */
    @Test
    @WithMockUser(roles = "ADMIN") // 管理者権限で認証
    void adminDashboard_ReturnsSuccessMessage() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string("管理者ダッシュボード - アクセス成功"));
    }

    /**
     * ユーザー管理画面へのアクセスが成功することを検証
     */
    @Test
    @WithMockUser(roles = "ADMIN") // 管理者権限で認証
    void manageUsers_ReturnsUserManagementMessage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().string("ユーザー管理画面"));
    }

    /**
     * 商品の新規作成が成功することを検証
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_WhenAdmin_ReturnsCreated() throws Exception {
        ProductDto productDto = new ProductDto();
        productDto.setSku("TEST-SKU123");
        productDto.setName("新商品名");

        Product createdProduct = new Product();
        createdProduct.setId(1L);
        createdProduct.setName("新商品名");

        when(productService.createProduct(any(ProductDto.class))).thenReturn(createdProduct);

        mockMvc.perform(post("/admin/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("新商品名"));
    }

    /**
     * 権限のないユーザーによる商品の新規作成が失敗することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void createProduct_WhenNotAdmin_ReturnsForbidden() throws Exception {
        ProductDto productDto = new ProductDto();
        productDto.setSku("TEST-SKU123");
        productDto.setName("新商品名");

        mockMvc.perform(post("/admin/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isForbidden());
    }

    /**
     * 商品の更新が成功することを検証
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_WhenProductExists_ReturnsOk() throws Exception {
        Long productId = 1L;
        Product productToUpdate = new Product();
        productToUpdate.setName("商品1");
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("商品2");

        when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/admin/products/{id}", productId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("商品2"));
    }

    /**
     * 存在しない商品の更新が失敗することを検証
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_WhenProductNotFound_ReturnsNotFound() throws Exception {
        Long productId = 99L;
        Product productToUpdate = new Product();
        productToUpdate.setName("商品1");

        when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(null);

        mockMvc.perform(put("/admin/products/{id}", productId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productToUpdate)))
                .andExpect(status().isNotFound());
    }

    /**
     * 商品の削除が成功することを検証
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_WhenProductExists_ReturnsNoContent() throws Exception {
        Long productId = 1L;
        doNothing().when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/admin/products/{id}", productId).with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * 商品の削除時にエラーが発生した場合を検証
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_WhenServiceThrowsException_ReturnsInternalServerError() throws Exception {
        Long productId = 1L;
        doThrow(new RuntimeException("データベースエラー")).when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/admin/products/{id}", productId).with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    /**
     * 全商品の削除が成功することを検証
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAllProducts_WhenCalledByAdmin_ReturnsNoContent() throws Exception {
        doNothing().when(productService).deleteAllProducts();

        mockMvc.perform(delete("/admin/products").with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * 全商品の削除時にエラーが発生した場合を検証
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAllProducts_WhenServiceThrowsException_ReturnsInternalServerError() throws Exception {
        doThrow(new RuntimeException("データベースエラー")).when(productService).deleteAllProducts();

        mockMvc.perform(delete("/admin/products").with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}