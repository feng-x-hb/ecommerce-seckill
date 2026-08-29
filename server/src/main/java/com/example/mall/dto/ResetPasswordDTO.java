package com.example.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求参数
 * 两种找回方式共用一个 DTO：
 *   - 账号名找回：传 username + newPassword（phone 留空）
 *   - 手机号找回：传 phone + newPassword（username 留空）
 */
public class ResetPasswordDTO {

    /** 账号名找回时传入 */
    private String username;

    /** 手机号找回时传入 */
    private String phone;

    /** 新密码，必填 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在 6~32 个字符之间")
    private String newPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
