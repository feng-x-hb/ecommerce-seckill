package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.SeckillService;
import com.example.mall.vo.SeckillItemVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 秒杀控制器（SeckillController）
 *
 * GET /api/seckill/list?activityId=1  公开，不需要登录
 * POST /api/seckill/buy              需要登录
 */
@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    /** 秒杀活动列表 */
    @GetMapping("/list")
    public Result<List<SeckillItemVO>> list(@RequestParam Long activityId) {
        return Result.success(seckillService.listSeckillItems(activityId));
    }

    /** 秒杀抢购 */
    @PostMapping("/buy")
    public Result<Map<String, String>> buy(@RequestAttribute("userId") Long userId,
                                           @RequestBody Map<String, Long> body) {
        Long seckillItemId = body.get("seckillItemId");
        String orderNo = seckillService.seckillBuy(userId, seckillItemId);
        return Result.success(Map.of("orderNo", orderNo));
    }
}
