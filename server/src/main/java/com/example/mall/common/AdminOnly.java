package com.example.mall.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员专属注解（@AdminOnly）
 *
 * 标记在 Controller 方法上，表示"只有管理员（role=2）才能调用此接口"。
 * 拦截器（LoginInterceptor）会在 preHandle 里检查：
 *   1. 目标方法是否带 @AdminOnly？
 *   2. 带了 → 当前用户 role 是否等于 2？
 *   3. role != 2 → 抛 ForbiddenException（403）。
 *
 * 为什么用注解而不是写死在拦截器配置里？
 *   注解紧跟在接口旁边，可读性高："这个接口只有管理员能用"一目了然；
 *   以后新管理接口加个 @AdminOnly 即可，不用反复改拦截器。
 */
@Target(ElementType.METHOD)   // 只能标在方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留，拦截器通过反射读取
public @interface AdminOnly {
}
