package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.Product;

/**
 * 商品数据访问层（ProductMapper）
 * 继承 BaseMapper 即拥有 CRUD 方法（selectById、selectList、insert、updateById、deleteById...）
 * 不需要写任何 SQL——MyBatis-Plus 根据实体类自动推断。
 */
public interface ProductMapper extends BaseMapper<Product> {
}
