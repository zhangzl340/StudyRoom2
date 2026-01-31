package com.studyroom.utils;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(400, message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未授权", null);
    }

    public static <T> Result<T> forbidden() {
        return new Result<>(403, "禁止访问", null);
    }

    public static <T> Result<T> notFound() {
        return new Result<>(404, "资源不存在", null);
    }

    public static <T> Result<T> serverError() {
        return new Result<>(500, "服务器内部错误", null);
    }

    public boolean isSuccess() {
        return code == 200;
    }

    public boolean isError() {
        return code != 200;
    }
}
