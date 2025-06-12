package com.hsbc.transaction.service.config;

import com.hsbc.transaction.service.common.DuplicateTransactionException;
import com.hsbc.transaction.service.common.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理类。
 * 使用@ControllerAdvice注解，使得该类可以处理所有Controller抛出的异常。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理TransactionNotFoundException，返回404 Not Found。
     * @param ex TransactionNotFoundException实例
     * @return 包含错误信息的ResponseEntity
     */
    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 设置HTTP状态码为404
    public ResponseEntity<Object> handleTransactionNotFoundException(TransactionNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * 处理DuplicateTransactionException，返回409 Conflict。
     * @param ex DuplicateTransactionException实例
     * @return 包含错误信息的ResponseEntity
     */
    @ExceptionHandler(DuplicateTransactionException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 设置HTTP状态码为409
    public ResponseEntity<Object> handleDuplicateTransactionException(DuplicateTransactionException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * 处理MethodArgumentNotValidException (JSR 303验证失败)，返回400 Bad Request。
     * @param ex MethodArgumentNotValidException实例
     * @return 包含错误信息的ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置HTTP状态码为400
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");

        // 收集所有验证错误信息
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        body.put("message", "请求参数验证失败: " + errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理所有未被特定处理的其他通用异常，返回500 Internal Server Error。
     * @param ex 任何Exception实例
     * @return 包含错误信息的ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置HTTP状态码为500
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "发生了一个未预期的错误: " + ex.getMessage());
        // 生产环境中通常不直接暴露详细异常信息，这里为了演示目的，可以包含。
        // ex.printStackTrace(); // 打印堆栈信息到控制台
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
