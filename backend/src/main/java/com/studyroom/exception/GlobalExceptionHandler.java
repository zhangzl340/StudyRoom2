package com.studyroom.exception;

import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("请求路径: {}", request.getRequestURI(), e);
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public Result<?> handleIOException(IOException e, HttpServletRequest request) {
        log.error("IO异常，请求路径: {}", request.getRequestURI(), e);
        return Result.error(500, "IO异常: " + e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.error("请求路径不存在，请求路径: {}", request.getRequestURI(), e);
        return Result.notFound();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("参数校验失败，请求路径: {}", request.getRequestURI(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail(400, message);
    }

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常，请求路径: {}", request.getRequestURI(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Result<?> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        log.error("未授权异常，请求路径: {}", request.getRequestURI(), e);
        return Result.error(401, e.getMessage());  // 传递具体的异常消息
    }

    @ExceptionHandler(ForbiddenException.class)
    public Result<?> handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        log.error("禁止访问异常，请求路径: {}", request.getRequestURI(), e);
        return Result.forbidden();
    }
}
