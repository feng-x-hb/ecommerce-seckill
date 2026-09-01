package com.example.mall.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 业务日志切面（BusinessLogAspect）
 *
 * 自动记录 Controller 方法的请求参数、执行耗时、返回结果。
 * 不需要每个 Controller 手动写 log.info(...)，一处配置全局生效。
 */
@Aspect
@Component
public class BusinessLogAspect {

    private static final Logger log = LoggerFactory.getLogger(BusinessLogAspect.class);

    /**
     * 拦截所有 Controller 层方法（service 层方法不拦截）
     */
    @Around("execution(* com.example.mall.controller..*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 获取当前请求信息
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String requestInfo = "";
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            requestInfo = request.getMethod() + " " + request.getRequestURI();
        }

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            // 只对关键业务操作打 INFO 日志，普通查询打 DEBUG
            if (isImportantOperation(className, methodName)) {
                log.info("[{}] {}.{}, 耗时={}ms, 参数={}",
                        requestInfo, className, methodName, elapsed,简化的Args(args));
            } else {
                log.debug("[{}] {}.{}, 耗时={}ms",
                        requestInfo, className, methodName, elapsed);
            }
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[{}] {}.{}, 耗时={}ms, 异常={}",
                    requestInfo, className, methodName, elapsed, ex.getMessage());
            throw ex;
        }
    }

    /** 判断是否为重要业务操作（需要 INFO 级别） */
    private boolean isImportantOperation(String className, String methodName) {
        return methodName.contains("login") || methodName.contains("Login")
                || methodName.contains("register") || methodName.contains("Register")
                || methodName.contains("create") || methodName.contains("Create")
                || methodName.contains("pay") || methodName.contains("Pay")
                || methodName.contains("seckill") || methodName.contains("Seckill")
                || methodName.contains("confirm") || methodName.contains("Confirm")
                || methodName.contains("cancel") || methodName.contains("Cancel")
                || methodName.contains("delete") || methodName.contains("Delete")
                || methodName.contains("batch");
    }

    /** 简化参数输出：只取关键字段，避免日志过长 */
    private String 简化的Args(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else if (arg instanceof String s) {
                sb.append(s.length() > 50 ? s.substring(0, 50) + "..." : s);
            } else if (arg instanceof Number || arg instanceof Boolean) {
                sb.append(arg);
            } else {
                // 对象类型：只打印类名
                sb.append(arg.getClass().getSimpleName());
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
