package com.portfolio.spring_ecommerce.exception;

/**
 * SKUが既に存在する場合の例外クラス
 */
public class SkuAlreadyExistsException extends RuntimeException {
    public SkuAlreadyExistsException(String message) {
        super(message);
    }
}