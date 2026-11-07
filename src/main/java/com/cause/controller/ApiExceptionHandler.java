package com.cause.controller;

import com.cause.service.NotFoundException;
import com.cause.service.RollbackDemoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(NotFoundException ex) {
        return new ApiError("not_found", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(IllegalArgumentException ex) {
        return new ApiError("bad_request", ex.getMessage());
    }

    @ExceptionHandler(RollbackDemoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError rolledBack(RollbackDemoException ex) {
        return new ApiError("rolled_back", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflict(DataIntegrityViolationException ex) {
        return new ApiError("conflict", "数据约束冲突");
    }

    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badSort(PropertyReferenceException ex) {
        return new ApiError("bad_request", "排序字段不存在");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError unreadable(HttpMessageNotReadableException ex) {
        return new ApiError("bad_request", "请求体不是合法 JSON");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError typeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ApiError("bad_request", "参数类型不正确");
    }

    public record ApiError(String error, String message) {
    }
}
