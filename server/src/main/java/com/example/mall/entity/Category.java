package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 分类实体类（Category）
 * 对应数据库表：category
 * 
 * 设计思路：
 * 分类是树形结构，用 parent_id 自关联实现。
 * parent_id = 0 表示顶级分类（没有父级），
 * parent_id = 某个 id 表示是那个分类的子分类。
 * 
 * 例如：
 *   手机数码（parent_id=0）
 *   ├── 智能手机（parent_id=手机数码的id）
 *   └── 平板电脑（parent_id=手机数码的id）
 */
@TableName("category")  // 指定对应的数据库表名
public class Category {

    /**
     * 分类 id（主键，自增）
     * 对应数据库字段：id
     * 用途：唯一标识一个分类，其他表通过 category_id 关联
     */
    @TableId(type = IdType.AUTO)  // 主键自增，插入后框架自动回填生成的 id
    private Long id;

    /**
     * 父分类 id
     * 对应数据库字段：parent_id
     * 设计：0 表示顶级分类（没有父级），其他值表示父分类的 id
     * 例如：智能手机的 parent_id = 手机数码的 id
     */
    private Long parentId;

    /**
     * 分类名称
     * 对应数据库字段：name
     * 用途：前端展示用，如"手机数码"、"智能手机"
     */
    private String name;

    /**
     * 排序号
     * 对应数据库字段：sort
     * 设计：数值越小越靠前，同级分类按此字段升序排列
     * 用途：前端展示时，sort 小的分类排在前面
     */
    private Integer sort;

    /**
     * 状态
     * 对应数据库字段：status
     * 设计：0 = 显示（正常），1 = 隐藏（禁用）
     * 用途：隐藏的分类前端不展示，但数据保留（方便以后恢复）
     */
    private Integer status;

    /**
     * 创建时间
     * 对应数据库字段：created_at
     * 设计：插入时自动填充，由 MyBatis-Plus 的 MetaObjectHandler 处理
     * 用途：记录分类创建的时间，用于审计和排序
     */
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充当前时间
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 对应数据库字段：updated_at
     * 设计：插入和更新时都自动填充
     * 用途：记录分类最后修改的时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时都自动填充
    private LocalDateTime updatedAt;

    // ===== getter / setter：Java 访问私有字段的"门" =====
    // 外部代码要读字段用 getXxx()，要改字段用 setXxx()
    // MyBatis-Plus 序列化/反序列化靠这些方法

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
