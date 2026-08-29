package com.example.mall.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.entity.User;
import com.example.mall.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 演示数据初始化器（DataInitializer）
 *
 * 它是干什么的？
 *   实现 Spring 的 CommandLineRunner 接口后，Spring 会在"应用完全启动、准备对外服务之前"，
 *   自动调用一次本类的 run() 方法。相当于应用刚睡醒、先跑一段我们指定的初始化逻辑。
 *
 *   为什么演示账号要在这里建，而不是写进 schema.sql？
 *   因为用户密码必须存 BCrypt 密文。在 .sql 里手写密文既难读又易错；
 *   这里直接调用项目里已有的 PasswordEncoder.encode("123456")，由代码生成合法密文，最稳妥。
 *
 *   为什么不会破坏你已有数据（幂等）？
 *   每次插入前先用 UserMapper 查"这个用户名存在吗"，不存在才插。
 *   所以：首次启动建好三个演示账号；之后无论重启多少次都跳过，你后来注册的真实账号毫发无损。
 *
 *   演示账号（密码统一 123456）：
 *     buyer001  角色 0 买家
 *     seller001 角色 1 商家
 *     admin001  角色 2 管理员
 */
@Component
public class DataInitializer implements CommandLineRunner {

    /** 用户表 Mapper：用来查重、插入 */
    private final UserMapper userMapper;

    /** BCrypt 加密器：用来把明文密码转成密文（和注册逻辑用同一个加密器，行为一致） */
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 三个演示账号的定义：用户名 / 角色 / 昵称
        insertIfAbsent("buyer001",  0, "演示买家");
        insertIfAbsent("seller001", 1, "演示商家");
        insertIfAbsent("admin001",  2, "演示管理员");
    }

    /**
     * 如果指定用户名的账号不存在，就创建一个（密码统一 123456，BCrypt 加密）。
     * 已存在则什么都不做——这就是"幂等"，保证重启不会重复建、不会清数据。
     */
    private void insertIfAbsent(String username, int role, String nickname) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count != null && count > 0) {
            return; // 已存在，跳过
        }

        // 生成不重复的占位手机号：138 + 角色偏移 + nanoTime 后6位
        String phone = "138" + String.format("%02d", role) + String.format("%06d", System.nanoTime() % 1000000);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(0);
        userMapper.insert(user);
    }
}
