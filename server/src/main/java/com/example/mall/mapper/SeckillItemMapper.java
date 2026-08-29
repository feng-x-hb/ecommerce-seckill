package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.SeckillItem;
import org.apache.ibatis.annotations.Update;

public interface SeckillItemMapper extends BaseMapper<SeckillItem> {

    /**
     * 秒杀扣库存（原子操作）
     * WHERE seckill_stock >= quantity 防超卖
     */
    @Update("UPDATE seckill_item SET seckill_stock = seckill_stock - #{quantity} WHERE id = #{id} AND seckill_stock >= #{quantity}")
    int decrStock(Long id, Integer quantity);
}
