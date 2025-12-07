package com.portfolio.spring_ecommerce.controller;

import com.portfolio.spring_ecommerce.dto.OrderDTO;
import com.portfolio.spring_ecommerce.dto.PaymentRequestDTO;
import com.portfolio.spring_ecommerce.dto.PaymentResponseDTO;
import com.portfolio.spring_ecommerce.model.Order;
import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.model.enums.OrderStatus;
import com.portfolio.spring_ecommerce.service.OrderService;
import com.portfolio.spring_ecommerce.service.PaymentService;
import com.portfolio.spring_ecommerce.util.GetAuthenticatedUser;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 支払い関連のエンドポイントを提供するコントローラー
 */
@RestController
@RequestMapping("/user/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final GetAuthenticatedUser getAuthenticatedUserUtil;

    public PaymentController(PaymentService paymentService, OrderService orderService, GetAuthenticatedUser getAuthenticatedUserUtil) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.getAuthenticatedUserUtil = getAuthenticatedUserUtil;
    }

    /**
     * 支払いインテントを作成するエンドポイント
     *
     * @param paymentRequest 支払いリクエストDTO
     * @return 支払いレスポンスDTOまたはエラーメッセージ
     */
    @PostMapping("/create-payment-intent")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentRequestDTO paymentRequest) {
        try {
            User user = getAuthenticatedUserUtil.getAuthenticatedUser();
            Optional<Order> orderOptional = orderService.getOrderById(paymentRequest.getOrderId());

            if (orderOptional.isEmpty() || !orderOptional.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("注文IDが見つからないか、アクセス権がありません。");
            }

            Order order = orderOptional.get();

            if (order.getStatus() != OrderStatus.PENDING) {
                return ResponseEntity.badRequest().body("支払いは保留中の注文に対してのみ行えます。");
            }

            OrderDTO orderDTO = new OrderDTO(order);
            PaymentIntent paymentIntent = paymentService.createPaymentIntent(orderDTO);

            return ResponseEntity.ok(new PaymentResponseDTO(paymentIntent.getClientSecret()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("支払いインテントの作成中にエラーが発生しました。");
        }
    }
}