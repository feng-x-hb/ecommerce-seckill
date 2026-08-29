package com.example.mall.common;

import com.example.mall.common.exception.BusinessException;
import com.example.mall.common.exception.ForbiddenException;
import com.example.mall.common.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器（GlobalExceptionHandler）
 * 加上 @RestControllerAdvice 后，它像一个"保安"：任何 Controller 抛出的异常，
 * 都会被这里对应的处理方法来接住，翻译成统一的 Result 返回给前端。
 * 这样前端永远不会看到 Java 的红色堆栈错误，只看到友好的中文提示。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 日志记录器：把真正的错误记下来，方便我们排查 */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常（BusinessException）
     * 场景："用户名已被注册""库存不足"这类我们自己主动抛出的异常。
     * 业务规则不允许的事，统一返回 400 + 具体提示，告诉前端"是你请求有问题"。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理未登录异常（UnauthorizedException）
     * 场景：没带 token、token 无效或已过期。
     * 返回 401（body 里的 code），告诉前端"请先登录"。
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Result<Void> handleUnauthorizedException(UnauthorizedException e) {
        return Result.error(401, e.getMessage());
    }

    /**
     * 处理无权限异常（ForbiddenException）
     * 场景：已登录但角色不够（如买家试图增删分类，应只有管理员）。
     * 返回 403（body 里的 code），告诉前端"权限不足"。
     */
    @ExceptionHandler(ForbiddenException.class)
    public Result<Void> handleForbiddenException(ForbiddenException e) {
        return Result.error(403, e.getMessage());
    }

    /**
     * 处理参数校验异常（MethodArgumentNotValidException）
     * 场景：前端传的 username 为空、手机号不是 11 位等，@Valid 校验不通过时抛出。
     * 这里取第一条校验错误信息返回（比如 DTO 里写的"手机号格式不正确"）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.isEmpty() ? "参数校验失败" : fieldErrors.get(0).getDefaultMessage();
        return Result.error(400, message);
    }

    /**
     * 兜底异常处理（Exception）
     * 场景：代码里没预料到的错误（数据库挂了、空指针等）。
     * 原则：把真实错误写进日志方便排查，但返回给前端只有一句笼统的"系统繁忙"，
     * 防止把内部细节（表名、堆栈）泄露给用户。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}
