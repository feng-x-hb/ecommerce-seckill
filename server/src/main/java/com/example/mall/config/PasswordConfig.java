package com.example.mall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置类
 * 把 BCrypt 加密器注册成 Spring 的一个 Bean（可复用的"工具人"）。
 * 好处：
 * 1. 整个应用只创建一个实例，由 Spring 统一管理；
 * 2. 代码里声明依赖 PasswordEncoder 接口，将来想换加密算法，只改这里。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
