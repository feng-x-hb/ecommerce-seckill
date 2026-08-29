package com.example.mall.vo;

import com.example.mall.entity.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类视图对象（CategoryVO）
 * 用于返回给前端的树形结构数据
 * 
 * 设计思路：
 * 前端需要的是树形结构（嵌套的 children），而不是平铺的列表。
 * 所以用递归的 VO 类：每个 CategoryVO 包含自己的 children 列表，
 * children 里的元素也是 CategoryVO，形成树形结构。
 * 
 * 例如：
 * {
 *   "id": 1, "name": "手机数码", "children": [
 *     { "id": 2, "name": "智能手机", "children": [] },
 *     { "id": 3, "name": "平板电脑", "children": [] }
 *   ]
 * }
 * 
 * 注意：这个 VO 和 Entity 不同。
 * Entity 对应数据库表（平铺结构），VO 对应前端展示（树形结构）。
 * Service 层把 Entity 转成 VO 再返回给前端。
 */
public class CategoryVO {

    /** 分类 id */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 排序号 */
    private Integer sort;

    /** 状态：0 显示 / 1 隐藏 */
    private Integer status;

    /**
     * 子分类列表（递归）
     * 每个子分类也是 CategoryVO，形成树形结构
     * 顶级分类的 children 有数据，叶子分类的 children 是空列表
     */
    private List<CategoryVO> children;

    // ===== 构造器 =====
    // 默认构造器：children 初始化为空列表，避免 null 导致前端报错
    public CategoryVO() {
        this.children = new ArrayList<>();
    }

    /**
     * 带参数的构造器
     * 从 Entity 复制字段，方便转换
     *
     * @param category 数据库实体
     */
    public CategoryVO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.sort = category.getSort();
        this.status = category.getStatus();
        this.children = new ArrayList<>();  // 初始化为空列表
    }

    // ===== getter / setter =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public List<CategoryVO> getChildren() { return children; }
    public void setChildren(List<CategoryVO> children) { this.children = children; }
}
