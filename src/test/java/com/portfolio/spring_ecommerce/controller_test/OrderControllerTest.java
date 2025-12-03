package com.portfolio.spring_ecommerce.controller_test;

import com.portfolio.spring_ecommerce.controller.OrderController;
import com.portfolio.spring_ecommerce.filter.JwtAuthenticationFilter;
import com.portfolio.spring_ecommerce.model.Order;
import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.model.enums.OrderStatus;
import com.portfolio.spring_ecommerce.service.OrderService;
import com.portfolio.spring_ecommerce.util.GetAuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OrderControllerの結合テストクラス
 */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc; // モックMVCを使用してコントローラのテストを実施

    @MockitoBean
    private OrderService orderService; // OrderServiceのモック

    @MockitoBean
    private GetAuthenticatedUser getAuthenticatedUserUtil; // 認証ユーザー取得ユーティリティのモック

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; // JWT認証フィルターのモック

    /**
     * カートから注文作成が成功することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void createOrderFromCart_success() throws Exception {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        Order testOrder = new Order();
        testOrder.setId(101L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalAmount(100.0);
        testOrder.setStatus(OrderStatus.PENDING);

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(orderService.createOrderFromCart(any(User.class))).thenReturn(testOrder);

        mockMvc.perform(post("/user/orders/create"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orders[0].id").value(testOrder.getId()))
                .andExpect(jsonPath("$.orders[0].status").value(testOrder.getStatus().toString()));
    }

    /**
     * カートが空の場合の注文作成失敗を検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void createOrderFromCart_failure_emptyCart() throws Exception {
        when(orderService.createOrderFromCart(any(User.class))).thenThrow(new IllegalArgumentException("カートが空のため、注文を作成できません。"));

        mockMvc.perform(post("/user/orders/create"))
                .andExpect(status().isBadRequest());
    }

    /**
     * ユーザーの注文一覧取得が成功することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void getOrdersForUser_success() throws Exception {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        Order testOrder = new Order();
        testOrder.setId(101L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalAmount(100.0);
        testOrder.setStatus(OrderStatus.PENDING);

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(testUser);

        List<Order> orders = Collections.singletonList(testOrder);
        when(orderService.getOrdersForUser(any(User.class))).thenReturn(orders);

        mockMvc.perform(get("/user/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].id").value(testOrder.getId()))
                .andExpect(jsonPath("$.orders[0].status").value(testOrder.getStatus().toString()));
    }

    /**
     * ユーザーの注文が存在しない場合の取得を検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void getOrdersForUser_success_noOrders() throws Exception {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        Order testOrder = new Order();
        testOrder.setId(101L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalAmount(100.0);

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(orderService.getOrdersForUser(any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").isEmpty());
    }

    /**
     * 注文ID指定で注文取得が成功することを検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void getOrderById_success() throws Exception {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        Order testOrder = new Order();
        testOrder.setId(101L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalAmount(100.0);
        testOrder.setStatus(OrderStatus.PENDING);

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(orderService.getOrderById(testOrder.getId())).thenReturn(Optional.of(testOrder));

        mockMvc.perform(get("/user/orders/{orderId}", testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].id").value(testOrder.getId()))
                .andExpect(jsonPath("$.orders[0].status").value(testOrder.getStatus().toString()));
    }

    /**
     * 存在しない注文ID指定時の取得失敗を検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void getOrderById_failure_notFound() throws Exception {
        Long nonExistentOrderId = 999L;
        when(orderService.getOrderById(nonExistentOrderId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/user/orders/{orderId}", nonExistentOrderId))
                .andExpect(status().isNotFound());
    }

    /**
     * 他ユーザーの注文取得時の失敗を検証
     */
    @Test
    @WithMockUser(roles = "USER")
    void getOrderById_failure_notOwner() throws Exception {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        Order testOrder = new Order();
        testOrder.setId(101L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalAmount(100.0);

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(testUser);

        User anotherUser = new User();
        anotherUser.setId(2L);
        testOrder.setUser(anotherUser);

        when(orderService.getOrderById(testOrder.getId())).thenReturn(Optional.of(testOrder));

        mockMvc.perform(get("/user/orders/{orderId}", testOrder.getId()))
                .andExpect(status().isNotFound());
    }
}