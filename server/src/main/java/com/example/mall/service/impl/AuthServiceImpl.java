package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.dto.RegisterDTO;
import com.example.mall.entity.User;
import com.example.mall.mapper.UserMapper;
import com.example.mall.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类（AuthServiceImpl）
 * 这就是业务层的"大脑"：Controller 把参数交进来，它负责判断规则、调用 Mapper 存库。
 * 分层铁律：本类只调 Mapper，不直接写 SQL；Mapper 只执行 SQL，不做业务判断。
 */
@Service
public class AuthServiceImpl implements AuthService {

    // 依赖注入：把 Mapper 和密码加密器"交给" Spring，由 Spring 自动塞进来
    // 用构造器注入是 Spring 官方推荐方式，好处是依赖一目了然、方便测试
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 注册流程（四步）：
     * 1. 查用户名是否被占用
     * 2. 查手机号是否被占用
     * 3. 密码加密 + 组装 User 实体
     * 4. 插入数据库，返回自增 id
     *
     * @Transactional：整个方法在同一个事务里，任何一步失败全部回滚，
     * 不会出现"查重通过但插入失败"导致的脏数据。
     */
    @Override
    @Transactional
    public Long register(RegisterDTO registerDTO) {
        // 第 1 步：用户名查重
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, registerDTO.getUsername()));
        if (usernameCount > 0) {
            throw new BusinessException("用户名已被注册");
        }

        // 第 2 步：手机号查重
        Long phoneCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, registerDTO.getPhone()));
        if (phoneCount > 0) {
            throw new BusinessException("手机号已被注册");
        }

        // 第 3 步：组装实体。密码绝不明文落库，先 BCrypt 加密
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        // 昵称默认等于用户名，用户以后可在个人中心修改
        user.setNickname(registerDTO.getUsername());
        // 角色 0 = 买家；状态 0 = 正常（这两列数据库也有默认值，这里显式赋值更清晰）
        user.setRole(0);
        user.setStatus(0);

        // 第 4 步：插入。created_at / updated_at 由 MyBatis-Plus 自动填充
        // 插入后框架会把数据库自增生成的 id 回填到 user.getId()
        userMapper.insert(user);
        return user.getId();
    }
}
