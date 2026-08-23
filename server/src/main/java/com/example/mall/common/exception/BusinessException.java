package com.example.mall.common.exception;

/**
 * 业务异常（BusinessException）
 * 用途：业务规则不允许某件事发生时（比如"用户名已存在"、"库存不足"），
 *       服务层就抛出这个异常，相当于吹哨子喊"这里有问题"。
 * 后续 Controller 层会有统一异常处理器接住它，转成统一的 JSON 返回给前端。
 * 继承 RuntimeException（运行时异常），这样调用处不需要强制写 try-catch。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
