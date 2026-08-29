package com.example.mall.vo;

/**
 * 当前用户信息视图对象（UserInfoVO）
 * 对应接口：GET /api/auth/me 的返回数据。
 *
 * 为什么单独建一个 VO，而不是直接返回 User 实体或复用 LoginVO？
 *   - 不返回 User 实体：实体含 password 等敏感字段，绝不能直出给前端；
 *   - 不复用 LoginVO：LoginVO 带 token，而 /me 是"凭已有 token 换用户信息"，不需要再给一个 token。
 * 所以这里只放前端真正要展示的字段：id / username / nickname / avatar / role。
 */
public class UserInfoVO {

    /** 用户 id */
    private Long id;

    /** 登录名 */
    private String username;

    /** 展示昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 角色：0买家 / 1商家 / 2管理员 */
    private Integer role;

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
}
