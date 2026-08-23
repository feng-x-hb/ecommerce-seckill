package com.example.mall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类（JwtProperties）
 * 作用：读取 application.yaml 里 jwt: 开头的配置（签名密钥、过期时间），
 *       让代码里不用把密钥"写死"。以后换密钥只改配置文件，不用动代码。
 *
 * @Component                 ：把这个类交给 Spring 管理，别的类可以直接注入它
 * @ConfigurationProperties   ：自动把配置文件里 jwt.xxx 的值映射到同名字段
 *（prefix = "jwt" 意思是只认 jwt: 下面那几行配置）
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * 签名密钥：用于给 token"盖章"和"验章"
     * 注意：HS256 算法要求密钥至少 32 字节，所以配置里要写一长串，不能写 "abc"
     */
    private String secret;

    /**
     * token 有效期（单位：小时）
     * 例：168 = 7 天。过期后这张"入场券"作废，用户必须重新登录
     */
    private Long expireHours;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpireHours() {
        return expireHours;
    }

    public void setExpireHours(Long expireHours) {
        this.expireHours = expireHours;
    }
}
