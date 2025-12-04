package com.portfolio.spring_ecommerce.exception;

/**
 * リソースが見つからない場合にスローされるカスタム例外クラス。
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}