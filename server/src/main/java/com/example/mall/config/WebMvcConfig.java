package com.example.mall.config;

import com.example.mall.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 配置类（WebMvcConfig）—— 保安调度表
 * 职责：把 LoginInterceptor 注册进 Spring MVC，告诉它：
 *       - 哪些路径要拦（/api/** 所有后端接口）
 *       - 哪些路径放行（白名单里的公开接口，见 application.yaml 的 mall.security.public-paths）
 *
 * 白名单机制：放行清单不再写死在代码里，而是读取 SecurityProperties 的配置。
 * 以后要新增公开接口（如商品列表、商品详情），只改 yaml 加一行，不用动这里。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final List<String> publicPaths;

    public WebMvcConfig(LoginInterceptor loginInterceptor, SecurityProperties securityProperties) {
        this.loginInterceptor = loginInterceptor;
        this.publicPaths = securityProperties.getPublicPaths();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 先拦下所有后端接口
        var registration = registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**");

        // 再把白名单里的每一个公开路径逐个放行
        // 例：/api/auth/register、/api/auth/login、/api/category/list
        for (String path : publicPaths) {
            registration.excludePathPatterns(path);
        }
    }
}
