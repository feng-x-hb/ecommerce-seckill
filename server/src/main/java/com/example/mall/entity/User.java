package com.example.mall.entity;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：user
 * 命名规则：数据库下划线(created_at) <-> Java驼峰(createdAt)
 * 由 MyBatis-Plus 的 map-underscore-to-camel-case 自动转换
 */
public class User {

    /** 主键，自增（对应 id） */
    private Long id;

    /** 用户名，登录用（对应 username） */
    private String username;

    /** 密码，BCrypt 加密后存储（对应 password） */
    private String password;

    /** 手机号，唯一（对应 phone） */
    private String phone;

    /** 昵称，可空（对应 nickname） */
    private String nickname;

    /** 头像 URL，可空（对应 avatar） */
    private String avatar;

    /** 角色：0买家 / 1商家 / 2管理员（对应 role） */
    private Integer role;

    /** 状态：0正常 / 1禁用（对应 status） */
    private Integer status;

    /** 创建时间，框架自动填充（对应 created_at） */
    private LocalDateTime createdAt;

    /** 更新时间，框架自动填充（对应 updated_at） */
    private LocalDateTime updatedAt;

    // ===== getter / setter：Java 访问私有字段的"门" =====
    // 外部代码要读字段用 getXxx()，要改字段用 setXxx()

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}