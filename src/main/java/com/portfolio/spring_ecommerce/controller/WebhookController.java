package com.portfolio.spring_ecommerce.controller;

import com.portfolio.spring_ecommerce.service.OrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
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
     * Stripeからのwebhookイベントを処理する
     * 
     * @param payload Stripeから送信される生のJSONペイロード
     * @param sigHeader Stripe-Signatureヘッダー（署名検証用）
     * @return 処理結果のレスポンス
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Webhookの署名を検証（セキュリティ対策）
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // 不正なリクエスト
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // イベントタイプに応じて処理
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentSuccess(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailure(event);
                break;
            default:
                // その他のイベントは無視
                return ResponseEntity.ok("Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    /**
     * 支払い成功時の処理
     */
    private void handlePaymentSuccess(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("PaymentIntentが見つかりません"));

        String paymentIntentId = paymentIntent.getId();
        
        try {
            orderService.markOrderAsPaid(paymentIntentId);
            System.out.println("注文が支払い済みに更新されました: " + paymentIntentId);
        } catch (Exception e) {
            System.err.println("注文の更新に失敗しました: " + e.getMessage());
        }
    }

    /**
     * 支払い失敗時の処理
     */
    private void handlePaymentFailure(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("PaymentIntentが見つかりません"));

        String paymentIntentId = paymentIntent.getId();
        
        try {
            orderService.cancelOrderAndRestoreInventory(paymentIntentId);
            System.out.println("注文がキャンセルされ、在庫が復元されました: " + paymentIntentId);
        } catch (Exception e) {
            System.err.println("注文のキャンセルに失敗しました: " + e.getMessage());
        }
    }
}