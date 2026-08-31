package com.example.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改账号密码 DTO（ChangeCredentialDTO）
 * 对应接口：PUT /api/auth/credential
 * 用户可修改登录账号（username）和密码，两者均需验证旧密码。
 */
public class ChangeCredentialDTO {

    /** 当前密码（用于验证身份，必须提供） */
    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;

    /** 新账号名（可选，修改时必须唯一） */
    private String newUsername;

    /** 新密码（可选，修改时需 6-20 位） */
    @Size(min = 6, max = 20, message = "新密码长度为 6-20 位")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
