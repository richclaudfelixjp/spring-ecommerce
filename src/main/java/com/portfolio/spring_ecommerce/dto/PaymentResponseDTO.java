package com.portfolio.spring_ecommerce.dto;

/**
 * 支払いレスポンス情報を表すDTO
 */
public class PaymentResponseDTO {
    private String clientSecret;

    public PaymentResponseDTO(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}