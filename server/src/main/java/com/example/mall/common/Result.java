package com.example.mall.common;

/**
 * 统一返回结果（Result）
 * 所有接口都返回这个盒子，前端永远只解析一种结构：
 * {
 *   "code": 200,      // 200成功 / 400参数错 / 401未登录 / 403无权限 / 500服务器错
 *   "message": "ok",  // 给前端的提示文字
 *   "data": ...       // 真正的业务数据（成功时才有）
 * }
 * 好处：前端写一套解析逻辑就够，不用每个接口单独猜返回格式。
 */
public class Result<T> {

    /** 业务状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    // 构造器设为 private：不允许外部直接 new，强制通过 success / error 两个入口创建，
    // 保证返回格式永远统一、不会有人漏填字段
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功：只需要业务数据，提示语固定为 "ok" */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "ok", data);
    }

    /** 失败：只需要状态码和提示语，data 为空 */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // ===== getter：JSON 序列化时 Jackjson 靠 getter 取值 =====

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
