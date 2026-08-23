package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.dto.LoginDTO;
import com.example.mall.dto.LoginVO;
import com.example.mall.dto.RegisterDTO;
import com.example.mall.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器（AuthController）—— 前台服务员
 * 职责红线：只负责"接请求 → 转给 Service → 把结果打包返回"，
 * 不写任何业务规则（查重、加密那些事都在 Service 里）。
 */
@RestController                            // 告诉 Spring：这是一个返回 JSON 的控制器
@RequestMapping("/api/auth")               // 本类所有接口的公共前缀，路径从这里开始
public class AuthController {

    /** 依赖注入：只依赖 AuthService 接口，不关心它底层怎么实现 */
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 注册接口：POST /api/auth/register
     * 对应接口设计文档：请求体传 username / password / phone，返回注册成功的用户 id。
     */
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterDTO registerDTO) {
        // 1. @RequestBody：把 JSON 转成 RegisterDTO
        // 2. @Valid：触发 DTO 上的 @NotBlank / @Size / @Pattern 校验，不通过会被全局异常处理器接住
        // 3. 把业务丢给 Service，返回结果包进统一的 Result 盒子
        Long userId = authService.register(registerDTO);
        return Result.success(userId);
    }

    /**
     * 登录接口：POST /api/auth/login
     * 请求体传 {account, password}；成功返回 token + 用户基本信息（LoginVO）。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // 1. @RequestBody：把 JSON 转成 LoginDTO（account 可以是用户名或手机号）
        // 2. @Valid：校验账号、密码不为空
        // 3. 业务全在 Service 里，Controller 只负责转手和打包
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }
}
