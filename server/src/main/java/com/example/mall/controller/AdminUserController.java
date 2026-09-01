package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.AdminOnly;
import com.example.mall.common.Result;
import com.example.mall.entity.User;
import com.example.mall.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理-用户", description = "管理员用户管理")
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {
    private final UserMapper userMapper;

    public AdminUserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Operation(summary = "用户列表查询")
    @GetMapping("/list")
    @AdminOnly
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return Result.success(userMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    @AdminOnly
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(400, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success(null);
    }
}
