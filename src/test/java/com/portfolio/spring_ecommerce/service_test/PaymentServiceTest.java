package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.dto.OrderDTO;
import com.portfolio.spring_ecommerce.model.Order;
import com.portfolio.spring_ecommerce.service.PaymentService;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * PaymentServiceの単体テストクラス。
 * Mockitoを使用してStripe API呼び出しをモック化し、支払いインテント作成ロジックを検証する。
 */
@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    /**
     * テスト前の初期化処理。
     * PaymentServiceのsecretKeyフィールドにテスト用の値を設定し、initメソッドを呼び出す。
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "secretKey", "test_secret_key");
        paymentService.init();
    }

    /**
     * PaymentServiceのcreatePaymentIntentメソッドの正常系テスト。
     * 正しいOrderDTOを渡した際に、期待通りのPaymentIntentが作成されることを検証する。
     */
    @Test
    void testCreatePaymentIntent_Success() throws StripeException {
        Order order = new Order();
        order.setId(1L);
        order.setTotalAmount(150.75);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderItems(Collections.emptyList());
        OrderDTO orderDTO = new OrderDTO(order);

        PaymentIntent mockPaymentIntent = new PaymentIntent();
        mockPaymentIntent.setId("pi_123");
        mockPaymentIntent.setAmount(15075L);
        mockPaymentIntent.setCurrency("usd");

        try (MockedStatic<PaymentIntent> mockedStatic = Mockito.mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockPaymentIntent);
            PaymentIntent result = paymentService.createPaymentIntent(orderDTO);

            assertNotNull(result);
            assertEquals(mockPaymentIntent.getId(), result.getId());
            assertEquals(15075L, result.getAmount());

            ArgumentCaptor<PaymentIntentCreateParams> captor = ArgumentCaptor.forClass(PaymentIntentCreateParams.class);
            mockedStatic.verify(() -> PaymentIntent.create(captor.capture()));
            PaymentIntentCreateParams capturedParams = captor.getValue();

            assertEquals(15075L, capturedParams.getAmount());
            assertEquals("usd", capturedParams.getCurrency());
            assertEquals("1", capturedParams.getMetadata().get("orderId"));
            assertTrue(capturedParams.getAutomaticPaymentMethods().getEnabled());
        }
    }

    /**
     * PaymentServiceのcreatePaymentIntentメソッドの異常系テスト。
     * Stripe API呼び出し中に例外が発生した場合に、適切に例外がスローされることを検証する。
     */
    @Test
    void testCreatePaymentIntent_StripeException() {
        Order order = new Order();
        order.setId(2L);
        order.setTotalAmount(200.0);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderItems(Collections.emptyList());
        OrderDTO orderDTO = new OrderDTO(order);

        StripeException stripeException = new ApiConnectionException("ネットワークエラー");

        try (MockedStatic<PaymentIntent> mockedStatic = Mockito.mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenThrow(stripeException);
            StripeException thrown = assertThrows(StripeException.class, () -> {
                paymentService.createPaymentIntent(orderDTO);
            });
            assertEquals("ネットワークエラー", thrown.getMessage());
        }
    }
}