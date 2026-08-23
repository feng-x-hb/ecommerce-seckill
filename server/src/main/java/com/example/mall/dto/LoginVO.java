package com.example.mall.dto;

/**
 * 登录返回数据（LoginVO）
 * VO（View Object）：专门给前端看的"视图对象"。
 * 登录成功后，把 token 和必要的用户信息打包返回，方便前端存储和展示。
 * 注意：绝对不把 password 放进来——谁都不该再看到密码。
 */
public class LoginVO {

    /** JWT 令牌：前端存起来，以后每个需要登录的请求都带上它 */
    private String token;

    /** 用户 id */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称：前端可以拿来显示，不用再查一次数据库 */
    private String nickname;

    /** 角色：0 买家 / 1 商家 / 2 管理员 */
    private Integer role;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
}
