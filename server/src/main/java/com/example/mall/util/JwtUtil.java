package com.example.mall.util;

import com.example.mall.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（JwtUtil）—— 盖章机 + 验章机
 * 职责：
 *   1. generateToken()：登录成功后，把用户身份信息写进 token 并签名（盖章）
 *   2. parseToken()   ：以后每次请求带 token 时，验签名、查过期（验章），拆出身份信息
 * 它是"无状态"的：服务器不存任何登录记录，信任全靠这一枚签名。
 */
@Component
public class JwtUtil {

    /** 从配置里读密钥和有效期（Spring 自动注入） */
    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 根据配置里的密钥字符串生成"印章"（SecretKey）
     * 注意：HS256 算法要求密钥至少 32 字节，太短会抛 WeakKeyException
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token（盖章）
     *
     * @param userId   用户 id（主键，最可靠的唯一标识）
     * @param username 用户名
     * @param role     角色（0 买家 / 1 商家 / 2 管理员）
     * @return 形如 xxx.yyy.zzz 的 JWT 字符串
     */
    public String generateToken(Long userId, String username, Integer role) {
        // 先把小时换算成毫秒：1 小时 = 60 分钟 * 60 秒 * 1000 毫秒
        long expireMillis = jwtProperties.getExpireHours() * 3600 * 1000;
        return Jwts.builder()
                .subject(String.valueOf(userId))                                 // 主体：放用户 id
                .claim("username", username)                                     // 自定义字段：用户名
                .claim("role", role)                                             // 自定义字段：角色
                .issuedAt(new Date())                                            // 签发时间（现在）
                .expiration(new Date(System.currentTimeMillis() + expireMillis)) // 过期时间（现在 + 7 天）
                .signWith(getSigningKey())                                       // 用密钥签名（盖章）
                .compact();                                                      // 压缩成最终的 JWT 字符串
    }

    /**
     * 解析并验证 token（验章）
     *
     * @param token 前端传来的 JWT 字符串
     * @return Claims（装着 userId、username、role 等身份信息）
     * 签名不对 / 已过期时，这里会直接抛异常，由全局异常处理器接住并转成 4xx 返回。
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // 用同一个密钥验章（不同密钥验不过）
                .build()
                .parseSignedClaims(token)    // 解析并校验签名 + 过期时间
                .getPayload();               // 拿出 payload 里的身份信息
    }
}
