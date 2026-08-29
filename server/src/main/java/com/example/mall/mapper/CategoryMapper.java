package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类数据访问层（CategoryMapper）
 * 继承 BaseMapper<Category> 后自动获得 category 表的增删改查方法
 * 
 * 为什么继承 BaseMapper 就不用手写 SQL？
 * MyBatis-Plus 会根据 Category 实体类自动生成对应的 SQL：
 * - insert(Category) → INSERT INTO category (...) VALUES (...)
 * - selectById(Long) → SELECT * FROM category WHERE id = ?
 * - selectList(Wrapper) → SELECT * FROM category WHERE ...
 * - updateById(Category) → UPDATE category SET ... WHERE id = ?
 * - deleteById(Long) → DELETE FROM category WHERE id = ?
 * 
 * 这些方法都是通用的，不需要自己写。
 * 只有复杂查询（比如查所有分类然后组装树）才需要自己写逻辑，
 * 但分类模块的查询都很简单，所以不需要自定义 SQL。
 */
@Mapper  // 告诉 Spring：这是一个 MyBatis Mapper 接口，要扫描并注册到 Spring 容器
public interface CategoryMapper extends BaseMapper<Category> {
}
