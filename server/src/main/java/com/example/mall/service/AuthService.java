package com.example.mall.service;

import com.example.mall.dto.RegisterDTO;
import com.example.mall.dto.LoginDTO;
import com.example.mall.dto.LoginVO;

/**
 * 认证服务接口（AuthService）
 * 职责：定义"认证模块"对外提供的业务能力。现阶段只有注册，以后登录（login）、
 *       获取当前用户（me）也会加在这里。
 * 拆成接口 + 实现类两个文件，是为了符合"面向接口编程"：
 *      上层（Controller）只认识 AuthService 接口，不关心底层是谁实现的。
 */
public interface AuthService {

    /**
     * 注册新用户
     *
     * @param registerDTO 注册参数（用户名、密码、手机号）
     * @return 新用户的自增 id
     */
    Long register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数（账号 = 用户名或手机号、密码）
     * @return 登录成功后的数据（token + 用户基本信息）
     */
    LoginVO login(LoginDTO loginDTO);
}
