package com.example.mall.dto;

/**
 * 个人资料更新 DTO（ProfileDTO）
 * 对应接口：PUT /api/auth/profile
 * 前端传什么字段就更新什么字段，均为可选。
 */
public class ProfileDTO {

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 个性签名 */
    private String signature;

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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
