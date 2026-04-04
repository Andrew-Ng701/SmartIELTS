package com.andrew.smartielts.common.handler;

import com.andrew.smartielts.common.constants.ApiMessageConstants;
import com.andrew.smartielts.common.resultDTO.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error(ApiMessageConstants.INVALID_REQUEST_BODY_FORMAT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        ObjectError error = e.getBindingResult().getAllErrors().stream().findFirst().orElse(null);
        String msg = error != null ? error.getDefaultMessage() : ApiMessageConstants.INVALID_REQUEST_BODY_FORMAT;
        return Result.error(msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        ObjectError error = e.getAllErrors().stream().findFirst().orElse(null);
        String msg = error != null ? error.getDefaultMessage() : ApiMessageConstants.INVALID_REQUEST_BODY_FORMAT;
        return Result.error(msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse(ApiMessageConstants.INVALID_REQUEST_BODY_FORMAT);
        return Result.error(msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        String msg = e.getMessage() != null && !e.getMessage().isBlank()
                ? e.getMessage()
                : ApiMessageConstants.INVALID_REQUEST_BODY_FORMAT;
        return Result.error(msg);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage() != null ? e.getMessage() : "System error");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error("System error");
    }
}