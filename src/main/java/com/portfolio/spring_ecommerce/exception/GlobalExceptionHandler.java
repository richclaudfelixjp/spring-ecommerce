package com.portfolio.spring_ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // 全コントローラーで発生する例外を一括して処理するためのアノテーション
public class GlobalExceptionHandler {

    /**
     * バリデーションエラー発生時の例外処理
     * @param ex バリデーション例外
     * @return フィールドごとのエラーメッセージとHTTPステータス400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // 各フィールドのエラー内容をMapに格納
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * 不正な引数が渡された場合の例外処理
     * @param ex 不正な引数例外
     * @return エラーメッセージとHTTPステータス400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * リソースが見つからなかった場合の例外処理
     * @param ex リソース未発見例外
     * @return エラーメッセージとHTTPステータス404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * SKUが既に存在する場合の例外処理
     * @param ex SKU重複例外
     * @return エラーメッセージとHTTPステータス409
     */
    @ExceptionHandler(SkuAlreadyExistsException.class)
    public ResponseEntity<Object> handleSkuAlreadyExistsException(SkuAlreadyExistsException ex, WebRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}