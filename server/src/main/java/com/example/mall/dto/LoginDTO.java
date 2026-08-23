package com.example.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求参数（LoginDTO）
 * 设计：账号只用一个字段 account，兼容"用户名"或"手机号"登录，
 *       到底按哪个查，由 Service 层决定（先用户名、后手机号）。
 */
public class LoginDTO {

    /** 登录账号：可以是用户名，也可以是手机号 */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 密码：明文传过来，后端用 BCrypt 和数据库里的密文"对暗号" */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在 6~32 个字符之间")
    private String password;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
