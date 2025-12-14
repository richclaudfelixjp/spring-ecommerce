package com.portfolio.spring_ecommerce.controller;

import com.portfolio.spring_ecommerce.service.OrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Stripeからのwebhookイベントを処理するコントローラー
 */
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final OrderService orderService;

    public WebhookController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Stripeのwebhookイベントを受信して処理するエンドポイント
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        byte[] payloadBytes;

        try {
            payloadBytes = request.getInputStream().readAllBytes();
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Failed to read request body");
        }

        String payload = new String(payloadBytes, StandardCharsets.UTF_8);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            System.err.println("❌ Invalid signature: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        System.out.println("📩 Received Stripe event: " + event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentSuccess(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailure(event);
                break;
            default:
                System.out.println("ℹ️ Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    /**
     * 支払い成功時の処理
     */
    private void handlePaymentSuccess(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new IllegalStateException("PaymentIntentが見つかりません"));

            String paymentIntentId = paymentIntent.getId();
            
            System.out.println("💳 Processing payment success for: " + paymentIntentId);
            orderService.markOrderAsPaid(paymentIntentId);
            System.out.println("✅ 注文が支払い済みに更新されました: " + paymentIntentId);
            
        } catch (Exception e) {
            System.err.println("❌ 注文の更新に失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 支払い失敗時の処理
     */
    private void handlePaymentFailure(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new IllegalStateException("PaymentIntentが見つかりません"));

            String paymentIntentId = paymentIntent.getId();
            
            System.out.println("💳 Processing payment failure for: " + paymentIntentId);
            orderService.cancelOrderAndRestoreInventory(paymentIntentId);
            System.out.println("✅ 注文がキャンセルされ、在庫が復元されました: " + paymentIntentId);
            
        } catch (Exception e) {
            System.err.println("❌ 注文のキャンセルに失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
}