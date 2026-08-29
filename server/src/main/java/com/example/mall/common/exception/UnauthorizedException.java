package com.example.mall.common.exception;

/**
 * 未登录异常（UnauthorizedException）
 * 场景：请求没带 token、token 无效或已过期时抛出。
 * 全局异常处理器会把 ta 翻译成统一的 401 返回给前端。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
