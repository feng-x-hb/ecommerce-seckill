package com.example.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 分类数据传输对象（CategoryDTO）
 * 用途：接收前端新增/修改分类时传入的参数，对齐"Entity 不进 Controller"原则。
 *
 * 为什么不直接用 Category 实体？
 *   - 实体有 id / createdAt / updatedAt 等不该由前端传入的字段；
 *   - 实体没有 @NotBlank 等校验注解，直接校验会侵入 Service 层；
 *   - DTO 是"前端→后端"的契约，清晰、可控、安全。
 *
 * 和 Category 的关系：Controller 收到 CategoryDTO 后，手动转成 Category 再传给 Service。
 */
public class CategoryDTO {

    /** 分类名称（必填） */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /** 父分类 id（可选，前端不传则默认 0 = 顶级分类） */
    @Min(value = 0, message = "父分类 id 不能为负数")
    private Long parentId;

    /** 排序号（可选，前端不传则默认 0） */
    private Integer sort;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
