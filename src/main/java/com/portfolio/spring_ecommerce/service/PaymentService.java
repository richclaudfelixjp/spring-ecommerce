package com.portfolio.spring_ecommerce.service;

import com.portfolio.spring_ecommerce.dto.OrderDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 支払い処理に関するビジネスロジックを処理するサービスクラス。
 */
@Service
public class PaymentService {

    @Value("${stripe.api.key.secret}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /**
     * Stripeで支払いインテントを作成する。
     * @param orderDTO 支払い対象の注文DTO
     * @return 作成されたPaymentIntentオブジェクト
     * @throws StripeException Stripe API呼び出し中にエラーが発生した場合
     */
    public PaymentIntent createPaymentIntent(OrderDTO orderDTO) throws StripeException {
        long amountInCents = (long) (orderDTO.getTotalAmount() * 100);

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency("usd")
                        .putMetadata("orderId", orderDTO.getId().toString())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                        )
                        .build();

        return PaymentIntent.create(params);
    }
}