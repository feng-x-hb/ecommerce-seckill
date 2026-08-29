package com.example.mall.interceptor;

import com.example.mall.common.AdminOnly;
import com.example.mall.common.exception.ForbiddenException;
import com.example.mall.common.exception.UnauthorizedException;
import com.example.mall.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器（LoginInterceptor）—— 门口保安（升级版）
 *
 * 基础职责：在请求进入 Controller 之前，检查请求头里有没有有效 token。
 *   有 → 解析出当前用户 id 和 role，放行；
 *   没有 / 无效 / 过期 → 抛未登录异常（全局处理器转成 401）。
 *
 * 新增职责（RBAC）：检查 @AdminOnly 注解。
 *   目标方法带了 @AdminOnly → 当前用户 role 必须等于 2（管理员），否则抛 403。
 *
 * 流程：请求 → 拦截器 preHandle() → Controller
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 第 1 步：从请求头拿 Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录");
        }

        // 第 2 步：去掉 "Bearer " 前缀，取纯 token
        String token = authHeader.substring(7);

        // 第 3 步：验章，拆出身份信息
        Claims claims = jwtUtil.parseToken(token);

        // 第 4 步：把 userId 和 role 都放进 request，供后续 Controller 使用
        request.setAttribute("userId", Long.parseLong(claims.getSubject()));
        request.setAttribute("role", claims.get("role", Integer.class));

        // 第 5 步（RBAC）：检查 @AdminOnly 注解
        // 只有当 handler 是 Controller 方法时才检查（静态资源等不检查）
        if (handler instanceof HandlerMethod handlerMethod) {
            // 先检查方法上有没有 @AdminOnly，再检查类上（类级别可做全局限制）
            boolean needAdmin = handlerMethod.hasMethodAnnotation(AdminOnly.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(AdminOnly.class);
            if (needAdmin) {
                Integer role = claims.get("role", Integer.class);
                if (role == null || role != 2) {
                    throw new ForbiddenException("权限不足，仅管理员可操作");
                }
            }
        }

        return true;
    }
}
