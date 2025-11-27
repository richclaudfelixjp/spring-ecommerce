package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.model.*;
import com.portfolio.spring_ecommerce.repository.*;
import com.portfolio.spring_ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CartServiceの単体テストクラス。
 * Mockitoを使用してリポジトリ層をモック化し、サービス層のロジックを検証する。
 */
class CartServiceTest {

    /**
     * CartRepositoryのモックオブジェクト。
     * カート情報の永続化をシミュレートする。
     */
    @Mock
    private CartRepository cartRepository;

    /**
     * CartItemRepositoryのモックオブジェクト。
     * カートアイテム情報の永続化をシミュレートする。
     */
    @Mock
    private CartItemRepository cartItemRepository;

    /**
     * テスト対象のCartServiceインスタンス。
     * 上記のモック（@Mock）がこのインスタンスに自動的に注入される。
     */
    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    /**
     * 各テスト実行前に共通のテストデータを初期化する。
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("testuser", "password");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        cartItem = new CartItem(product, 2);
        cartItem.setId(10L);

        cart = new Cart(user, new ArrayList<>());
        cart.setId(5L);
        cart.getItems().add(cartItem);
    }

    /**
     * getOrCreateCartメソッドのテスト（カートが既に存在する場合）。
     * 既存のカートが正しく取得されることを検証する。
     */
    @Test
    void testGetOrCreateCart_CartExists() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        Cart result = cartService.getOrCreateCart(user);
        assertEquals(cart, result);
    }

    /**
     * getOrCreateCartメソッドのテスト（カートが存在しない場合）。
     * 新規カートが正しく作成されることを検証する。
     */
    @Test
    void testGetOrCreateCart_CartDoesNotExist() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Cart result = cartService.getOrCreateCart(user);
        assertEquals(user, result.getUser());
        assertTrue(result.getItems().isEmpty());
    }

    /**
     * addItemToCartメソッドのテスト。
     * 商品がカートに正しく追加されることを検証する。
     */
    @Test
    void testAddItemToCart() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addItemToCart(user, product, 2);
        assertTrue(result.getItems().stream().anyMatch(i -> i.getProduct().equals(product) && i.getQuantity() == 2));
    }

    /**
     * removeItemFromCartメソッドのテスト。
     * カートアイテムが正しく削除されることを検証する。
     */
    @Test
    void testRemoveItemFromCart() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        doNothing().when(cartItemRepository).deleteById(cartItem.getId());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.removeItemFromCart(user, cartItem.getId());
        assertTrue(result.getItems().isEmpty());
    }

    /**
     * updateCartItemQuantityメソッドのテスト。
     * カートアイテムの数量が正しく更新されることを検証する。
     */
    @Test
    void testUpdateCartItemQuantity() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.updateCartItemQuantity(user, cartItem.getId(), 4);
        assertEquals(4, result.getItems().get(0).getQuantity());
    }
}