package com.example.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求参数（DTO：Data Transfer Object，数据传输对象）
 * 作用：接收前端传来的参数，并在这里做第一道参数校验。
 * 为什么不直接传 User 实体？因为实体对应数据库整张表（还有 role、status、时间等字段），
 * 前端只需要传 3 个字段，多传会造成混淆和安全问题。
 */
public class RegisterDTO {

    /** 用户名，登录用，唯一 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3~20 个字符之间")
    private String username;

    /** 密码，明文传输，后端 BCrypt 加密后再存库 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在 6~32 个字符之间")
    private String password;

    /** 手机号，可选，中国大陆 11 位手机号 */
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    /** 角色：0=买家（默认），1=商家 */
    private Integer role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
}
