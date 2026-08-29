package com.example.mall.service;

import com.example.mall.vo.SeckillItemVO;

import java.util.List;

/**
 * 秒杀服务接口（SeckillService）
 */
public interface SeckillService {

    /** 获取秒杀活动列表 */
    List<SeckillItemVO> listSeckillItems(Long activityId);

    /** 秒杀抢购 */
    String seckillBuy(Long userId, Long seckillItemId);
}
