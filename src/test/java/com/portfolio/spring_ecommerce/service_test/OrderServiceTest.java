package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.model.*;
import com.portfolio.spring_ecommerce.model.enums.OrderStatus;
import com.portfolio.spring_ecommerce.repository.CartRepository;
import com.portfolio.spring_ecommerce.repository.OrderRepository;
import com.portfolio.spring_ecommerce.repository.ProductRepository;
import com.portfolio.spring_ecommerce.service.CartService;
import com.portfolio.spring_ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderServiceの単体テストクラス。
 * Mockitoを使用してリポジトリ層・サービス層をモック化し、注文作成ロジックを検証する。
 */
class OrderServiceTest {

    /**
     * OrderRepositoryのモックオブジェクト。
     * 注文情報の永続化をシミュレートする。
     */
    @Mock
    private OrderRepository orderRepository;

    /**
     * CartRepositoryのモックオブジェクト。
     * カート情報の永続化をシミュレートする。
     */
    @Mock
    private CartRepository cartRepository;

    /**
     * ProductRepositoryのモックオブジェクト。
     * 商品情報の永続化をシミュレートする。
     */
    @Mock
    private ProductRepository productRepository;

    /**
     * CartServiceのモックオブジェクト。
     * カート取得・操作をシミュレートする。
     */
    @Mock
    private CartService cartService;

    /**
     * テスト対象のOrderServiceインスタンス。
     * 上記のモック（@Mock）がこのインスタンスに自動的に注入される。
     */
    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product1;
    private Product product2;

    /**
     * 各テスト実行前に共通のテストデータを初期化する。
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        cart = new Cart();
        cart.setUser(user);
        cart.setId(1L);
        cart.setItems(new ArrayList<>());

        product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setUnitPrice(new BigDecimal("10.00"));
        product1.setUnitsInStock(10);

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setUnitPrice(new BigDecimal("20.00"));
        product2.setUnitsInStock(5);
    }

    /**
     * createOrderFromCartメソッドのテスト（正常系）。
     * カートから注文が正しく作成されることを検証する。
     */
    @Test
    void testCreateOrderFromCart_Success() {
        CartItem cartItem1 = new CartItem(product1, 2);
        CartItem cartItem2 = new CartItem(product2, 3);
        cart.getItems().add(cartItem1);
        cart.getItems().add(cartItem2);

        when(cartService.getOrCreateCart(user)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order createdOrder = orderService.createOrderFromCart(user);

        assertNotNull(createdOrder);
        assertEquals(user, createdOrder.getUser());
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
        assertEquals(2, createdOrder.getOrderItems().size());
        assertEquals(80.0, createdOrder.getTotalAmount());

        verify(cartService, times(1)).getOrCreateCart(user);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, times(2)).save(any(Product.class));
        verify(cartRepository, times(1)).save(any(Cart.class));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());
        List<Product> savedProducts = productCaptor.getAllValues();
        assertEquals(8, savedProducts.get(0).getUnitsInStock());
        assertEquals(2, savedProducts.get(1).getUnitsInStock());

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        assertTrue(cartCaptor.getValue().getItems().isEmpty());
    }

    /**
     * createOrderFromCartメソッドのテスト（カートが空の場合）。
     * 空のカートから注文作成時に例外が発生することを検証する。
     */
    @Test
    void testCreateOrderFromCart_EmptyCart_ThrowsException() {
        when(cartService.getOrCreateCart(user)).thenReturn(cart);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.createOrderFromCart(user);
        });

        assertEquals("カートが空のため、注文を作成できません。", exception.getMessage());

        verify(orderRepository, never()).save(any());
        verify(productRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    /**
     * createOrderFromCartメソッドのテスト（在庫不足の場合）。
     * 商品の在庫が不足している場合に例外が発生することを検証する。
     */
    @Test
    void testCreateOrderFromCart_InsufficientStock_ThrowsException() {
        CartItem cartItem = new CartItem(product1, 11);
        cart.getItems().add(cartItem);

        when(cartService.getOrCreateCart(user)).thenReturn(cart);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.createOrderFromCart(user);
        });

        assertEquals("在庫が不足しています: " + product1.getName(), exception.getMessage());

        verify(orderRepository, never()).save(any());
        verify(productRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }
}