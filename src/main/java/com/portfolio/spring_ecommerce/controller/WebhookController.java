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
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        Event event;

        try {
            // Webhookの署名を検証（セキュリティ対策）
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // 不正なリクエスト
            System.err.println("❌ Invalid signature: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            // その他のパースエラー（pingイベントなど）
            System.err.println("⚠️ Webhook parsing error: " + e.getMessage());
            // 200を返してStripeがリトライしないようにする
            return ResponseEntity.ok("Event received but couldn't parse: " + e.getMessage());
        }

        // イベントタイプに応じて処理
        String eventType = event.getType();
        System.out.println("📩 Received Stripe event: " + eventType);

        switch (eventType) {
            case "payment_intent.succeeded":
                handlePaymentSuccess(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailure(event);
                break;
            default:
                // その他のイベントは無視（pingなど）
                System.out.println("ℹ️ Unhandled event type: " + eventType);
                return ResponseEntity.ok("Unhandled event type: " + eventType);
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