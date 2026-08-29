package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.AdminOnly;
import com.example.mall.common.Result;
import com.example.mall.entity.SeckillActivity;
import com.example.mall.entity.SeckillItem;
import com.example.mall.entity.Sku;
import com.example.mall.mapper.SeckillActivityMapper;
import com.example.mall.mapper.SeckillItemMapper;
import com.example.mall.mapper.SkuMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员秒杀活动管理控制器
 */
@RestController
@RequestMapping("/api/admin/seckill")
public class AdminSeckillController {

    private final SeckillActivityMapper activityMapper;
    private final SeckillItemMapper itemMapper;
    private final SkuMapper skuMapper;

    public AdminSeckillController(SeckillActivityMapper activityMapper, SeckillItemMapper itemMapper, SkuMapper skuMapper) {
        this.activityMapper = activityMapper;
        this.itemMapper = itemMapper;
        this.skuMapper = skuMapper;
    }

    /** 分页查询所有秒杀活动 */
    @GetMapping("/activity/list")
    @AdminOnly
    public Result<Page<SeckillActivity>> activityList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SeckillActivity> result = activityMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<SeckillActivity>().orderByDesc(SeckillActivity::getCreatedAt));
        return Result.success(result);
    }

    /** 获取活动详情 */
    @GetMapping("/activity/{id}")
    @AdminOnly
    public Result<SeckillActivity> activityDetail(@PathVariable Long id) {
        SeckillActivity activity = activityMapper.selectById(id);
        if (activity == null) return Result.error(400, "活动不存在");
        return Result.success(activity);
    }

    /** 创建秒杀活动 */
    @PostMapping("/activity")
    @AdminOnly
    public Result<SeckillActivity> createActivity(@RequestBody SeckillActivity activity) {
        activity.setStatus(0);
        activityMapper.insert(activity);
        return Result.success(activity);
    }

    /** 更新秒杀活动 */
    @PutMapping("/activity/{id}")
    @AdminOnly
    public Result<Void> updateActivity(@PathVariable Long id, @RequestBody SeckillActivity activity) {
        activity.setId(id);
        activityMapper.updateById(activity);
        return Result.success(null);
    }

    /** 删除秒杀活动 */
    @DeleteMapping("/activity/{id}")
    @AdminOnly
    public Result<Void> deleteActivity(@PathVariable Long id) {
        // 同时删除关联的秒杀商品
        itemMapper.delete(new LambdaQueryWrapper<SeckillItem>().eq(SeckillItem::getActivityId, id));
        activityMapper.deleteById(id);
        return Result.success(null);
    }

    /** 获取活动下的秒杀商品列表 */
    @GetMapping("/activity/{activityId}/items")
    @AdminOnly
    public Result<List<SeckillItem>> itemList(@PathVariable Long activityId) {
        List<SeckillItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<SeckillItem>().eq(SeckillItem::getActivityId, activityId));
        return Result.success(items);
    }

    /** 添加秒杀商品到活动 */
    @PostMapping("/activity/{activityId}/item")
    @AdminOnly
    public Result<SeckillItem> addItem(@PathVariable Long activityId, @RequestBody SeckillItem item) {
        item.setActivityId(activityId);
        // 填充原价
        Sku sku = skuMapper.selectById(item.getSkuId());
        if (sku != null) {
            item.setNormalPrice(sku.getPrice());
        }
        itemMapper.insert(item);
        return Result.success(item);
    }

    /** 删除秒杀商品 */
    @DeleteMapping("/item/{id}")
    @AdminOnly
    public Result<Void> deleteItem(@PathVariable Long id) {
        itemMapper.deleteById(id);
        return Result.success(null);
    }

    /** 获取所有 SKU（供选择添加秒杀商品） */
    @GetMapping("/sku/list")
    @AdminOnly
    public Result<List<Sku>> allSkuList() {
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getStatus, 1));
        return Result.success(skus);
    }
}
