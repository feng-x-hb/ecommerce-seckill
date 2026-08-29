package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.Sku;
import org.apache.ibatis.annotations.Update;

/**
 * SKU 数据访问层（SkuMapper）
 * 继承 BaseMapper 即拥有 CRUD 方法。
 * 额外提供库存原子操作方法（防超卖核心）。
 */
public interface SkuMapper extends BaseMapper<Sku> {

    /**
     * 扣减库存（防超卖）
     * WHERE stock >= quantity 保证不会扣成负数
     * affected rows = 0 说明库存不足，调用方应抛异常
     */
    @Update("UPDATE sku SET stock = stock - #{quantity} WHERE id = #{skuId} AND stock >= #{quantity}")
    int decrStock(Long skuId, Integer quantity);

    /**
     * 恢复库存（取消订单时调用）
     */
    @Update("UPDATE sku SET stock = stock + #{quantity} WHERE id = #{skuId}")
    int incrStock(Long skuId, Integer quantity);
}
