package com.example.mall.common.exception;

/**
 * 无权限异常（ForbiddenException）
 * 场景：已登录，但角色不够（如买家试图删分类）。
 * 全局异常处理器捕获后返回 HTTP 403 + 提示信息，前端据此显示"无权限"。
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
