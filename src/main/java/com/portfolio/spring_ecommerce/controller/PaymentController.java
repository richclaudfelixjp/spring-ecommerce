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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            
            // 🆕 PaymentIntent IDを注文に保存
            order.setPaymentIntentId(paymentIntent.getId());
            orderService.saveOrder(order); // この行も追加が必要

            return ResponseEntity.ok(new PaymentResponseDTO(paymentIntent.getClientSecret()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("支払いインテントの作成中にエラーが発生しました。");
        }
    }

    /**
     * 既存の保留中注文の支払いを再開する
     * 
     * @param orderId 注文ID
     * @return 既存または新規のPaymentIntentのclientSecret
     */
    @GetMapping("/retry-payment/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> retryPayment(@PathVariable Long orderId) {
        try {
            User user = getAuthenticatedUserUtil.getAuthenticatedUser();
            Optional<Order> orderOptional = orderService.getOrderById(orderId);

            // アクセス権の確認
            if (orderOptional.isEmpty() || !orderOptional.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("注文が見つからないか、アクセス権がありません。");
            }

            Order order = orderOptional.get();

            // 支払いは保留中の注文に対してのみ行えます
            if (order.getStatus() != OrderStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body("支払いは保留中の注文に対してのみ行えます。現在のステータス: " + order.getStatus());
            }

            // 既存のPaymentIntentを取得して再利用
            if (order.getPaymentIntentId() != null) {
                try {
                    PaymentIntent existingPI = PaymentIntent.retrieve(order.getPaymentIntentId());
                    
                    String status = existingPI.getStatus();
                    if (status.equals("requires_payment_method") || 
                        status.equals("requires_confirmation") ||
                        status.equals("requires_action")) {
                        
                        return ResponseEntity.ok(new PaymentResponseDTO(existingPI.getClientSecret()));
                    }
                    
                } catch (StripeException e) {
                }
            }

            // 新しいPaymentIntentを作成
            OrderDTO orderDTO = new OrderDTO(order);
            PaymentIntent paymentIntent = paymentService.createPaymentIntent(orderDTO);
            
            // PaymentIntent IDを注文に保存
            order.setPaymentIntentId(paymentIntent.getId());
            orderService.saveOrder(order);

            return ResponseEntity.ok(new PaymentResponseDTO(paymentIntent.getClientSecret()));
            
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("支払いインテントの取得中にエラーが発生しました。");
        }
    }
}