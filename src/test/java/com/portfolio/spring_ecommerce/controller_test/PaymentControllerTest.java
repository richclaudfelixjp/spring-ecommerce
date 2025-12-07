package com.portfolio.spring_ecommerce.controller_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.spring_ecommerce.controller.PaymentController;
import com.portfolio.spring_ecommerce.dto.OrderDTO;
import com.portfolio.spring_ecommerce.dto.PaymentRequestDTO;
import com.portfolio.spring_ecommerce.filter.JwtAuthenticationFilter;
import com.portfolio.spring_ecommerce.model.Order;
import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.model.enums.OrderStatus;
import com.portfolio.spring_ecommerce.service.OrderService;
import com.portfolio.spring_ecommerce.service.PaymentService;
import com.portfolio.spring_ecommerce.util.GetAuthenticatedUser;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * PaymentControllerの結合テスト
 */
@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private GetAuthenticatedUser getAuthenticatedUserUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User user;
    private Order order;
    private PaymentRequestDTO paymentRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(1000.0);

        paymentRequest = new PaymentRequestDTO();
        paymentRequest.setOrderId(order.getId());
    }

    /**
     * 支払いインテント作成の成功ケースのテスト
     */
    @Test
    @WithMockUser(roles = "USER")
    void createPaymentIntent_Success() throws Exception {
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setClientSecret("test_client_secret");

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(orderService.getOrderById(order.getId())).thenReturn(Optional.of(order));
        when(paymentService.createPaymentIntent(any(OrderDTO.class))).thenReturn(paymentIntent);

        mockMvc.perform(post("/user/payment/create-payment-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSecret").value("test_client_secret"));
    }

    /**
     * 注文が見つからない場合のテスト
     */
    @Test
    @WithMockUser(roles = "USER")
    void createPaymentIntent_OrderNotFound() throws Exception {
        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(orderService.getOrderById(order.getId())).thenReturn(Optional.empty());

        mockMvc.perform(post("/user/payment/create-payment-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("注文IDが見つからないか、アクセス権がありません。"));
    }

    /**
     * 注文のステータスがPENDINGでない場合のテスト
     */
    @Test
    @WithMockUser(roles = "USER")
    void createPaymentIntent_InvalidOrderStatus() throws Exception {
        order.setStatus(OrderStatus.PAID);

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(orderService.getOrderById(order.getId())).thenReturn(Optional.of(order));

        mockMvc.perform(post("/user/payment/create-payment-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("支払いは保留中の注文に対してのみ行えます。"));
    }

    /**
     * 注文が他のユーザーのものである場合のテスト
     */
    @Test
    @WithMockUser(roles = "USER")
    void createPaymentIntent_Forbidden() throws Exception {
        User anotherUser = new User();
        anotherUser.setId(2L);
        order.setUser(anotherUser);

        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(orderService.getOrderById(order.getId())).thenReturn(Optional.of(order));

        mockMvc.perform(post("/user/payment/create-payment-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("注文IDが見つからないか、アクセス権がありません。"));
    }

    /**
     * StripeException発生時のテスト
     */
    @Test
    @WithMockUser(roles = "USER")
    void createPaymentIntent_StripeException() throws Exception {
        when(getAuthenticatedUserUtil.getAuthenticatedUser()).thenReturn(user);
        when(orderService.getOrderById(order.getId())).thenReturn(Optional.of(order));
        when(paymentService.createPaymentIntent(any(OrderDTO.class))).thenThrow(new StripeException("Stripe error", "req_123", null, 500) {});

        mockMvc.perform(post("/user/payment/create-payment-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("支払いインテントの作成中にエラーが発生しました。"));
    }

    /**
     * 認証されていない場合のテスト
     */
    @Test
    void createPaymentIntent_Unauthorized() throws Exception {
        mockMvc.perform(post("/user/payment/create-payment-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isForbidden());
    }
}