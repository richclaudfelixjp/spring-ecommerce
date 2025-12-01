package com.portfolio.spring_ecommerce.controller_test;

import com.portfolio.spring_ecommerce.controller.CartController;
import com.portfolio.spring_ecommerce.model.Cart;
import com.portfolio.spring_ecommerce.model.CartItem;
import com.portfolio.spring_ecommerce.model.Product;
import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.CartItemRepository;
import com.portfolio.spring_ecommerce.repository.ProductRepository;
import com.portfolio.spring_ecommerce.service.CartService;
import com.portfolio.spring_ecommerce.util.GetAuthenticatedUser;
import com.portfolio.spring_ecommerce.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

/**
 * CartControllerの結合テストクラス
 */
@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private GetAuthenticatedUser getAuthenticatedUserUtil;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * ユーザーのカート取得が成功することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void getCart_Success() throws Exception {
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setItems(Collections.singletonList(cartItem));

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(cartService.getOrCreateCart(user)).thenReturn(cart);

        mockMvc.perform(get("/user/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(cartItem.getId()))
                .andExpect(jsonPath("$.username").value(user.getUsername()));

        verify(getAuthenticatedUserUtil).getAuthenticatedUser();
        verify(cartService).getOrCreateCart(user);
    }

    /**
     * ユーザーが見つからない場合にカート取得が失敗することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void getCart_UserNotFound_ReturnsNotFound() throws Exception {
        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(null);

        mockMvc.perform(get("/user/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(getAuthenticatedUserUtil).getAuthenticatedUser();
    }

    /**
     * カートへの商品追加が成功することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void addItem_Success() throws Exception {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(100L);
        product.setStatus(true);
        product.setUnitsInStock(10);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(Collections.emptyList());

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);

        Cart updatedCart = new Cart();
        updatedCart.setId(20L);
        updatedCart.setItems(Collections.singletonList(cartItem));


        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartService.getOrCreateCart(user)).thenReturn(cart);
        when(cartService.addItemToCart(user, product, 2)).thenReturn(updatedCart);

        mockMvc.perform(post("/user/cart/add")
                        .param("productId", "100")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(cartItem.getId()));

        verify(cartService).addItemToCart(user, product, 2);
    }

    /**
     * 存在しない商品をカートに追加しようとした場合に失敗することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void addItem_ProductNotFound_ReturnsBadRequest() throws Exception {
        User user = new User();
        user.setId(1L);

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(post("/user/cart/add")
                        .param("productId", "999")
                        .param("quantity", "1"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(), any(), anyInt());
    }

    /**
     * カート内の商品の数量更新が成功することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void updateQuantity_Success() throws Exception {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setUnitsInStock(10);

        CartItem cartItem = new CartItem();
        cartItem.setId(5L);
        cartItem.setProduct(product);

        Cart updatedCart = new Cart();
        updatedCart.setId(20L);
        updatedCart.setItems(Collections.singletonList(cartItem));

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(cartItem));
        when(cartService.updateCartItemQuantity(user, 5L, 3)).thenReturn(updatedCart);

        mockMvc.perform(put("/user/cart/update/5")
                        .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(cartItem.getId()));

        verify(cartService).updateCartItemQuantity(user, 5L, 3);
    }

    /**
     * 在庫不足の場合に数量更新が失敗することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void updateQuantity_InsufficientStock_ThrowsException() throws Exception {
        User user = new User();
        user.setId(1L);
        Product product = new Product();
        product.setUnitsInStock(5);
        CartItem cartItem = new CartItem();
        cartItem.setId(5L);
        cartItem.setProduct(product);

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(cartItem));

        mockMvc.perform(put("/user/cart/update/5")
                        .param("quantity", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"error\":\"在庫が不足しています。\"}"));

        verify(cartService, never()).updateCartItemQuantity(any(), anyLong(), anyInt());
    }

    /**
     * カートからの商品削除が成功することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void removeItem_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        CartItem cartItem = new CartItem();
        cartItem.setId(5L);
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(Collections.singletonList(cartItem));
        Cart updatedCart = new Cart();
        updatedCart.setId(20L);
        updatedCart.setItems(Collections.singletonList(cartItem));

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(cartService.getOrCreateCart(user)).thenReturn(cart);
        when(cartService.removeItemFromCart(user, 5L)).thenReturn(updatedCart);

        mockMvc.perform(delete("/user/cart/remove/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(cartItem.getId()));

        verify(cartService).removeItemFromCart(user, 5L);
    }

    /**
     * ユーザーのカートにない商品を削除しようとした場合に失敗することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void removeItem_NotInUserCart_ThrowsException() throws Exception {
        User user = new User();
        user.setId(1L);
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(Collections.emptyList());

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(cartService.getOrCreateCart(user)).thenReturn(cart);

        mockMvc.perform(delete("/user/cart/remove/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"error\":\"このカートアイテムはあなたのカートに存在しません。\"}"));

        verify(cartService, never()).removeItemFromCart(any(), anyLong());
    }
}