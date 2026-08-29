package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.dto.LoginDTO;
import com.example.mall.dto.LoginVO;
import com.example.mall.dto.RegisterDTO;
import com.example.mall.entity.User;
import com.example.mall.mapper.UserMapper;
import com.example.mall.util.JwtUtil;
import com.example.mall.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试
 *
 * 测试策略：用 Mockito 模拟 UserMapper（不连数据库），只验证业务逻辑。
 *   - 注册：用户名重复 → 抛异常
 *   - 登录：密码错误 → 抛异常；账号不存在 → 抛异常；正常 → 返回 token
 *
 * 为什么用 @ExtendWith(MockitoExtension.class) 而不是 @SpringBootTest？
 *   单元测试：只测一个 Service，Mock 掉所有依赖，跑得快、隔离好；
 *   集成测试：需要 @SpringBootTest + 真实数据库，后面做接口测试时再写。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    /** 被测对象（Spring 会自动把 @Mock 注入进来） */
    @InjectMocks
    private AuthServiceImpl authService;

    /** 模拟 Mapper：不连数据库，只返回我们预设的值 */
    @Mock
    private UserMapper userMapper;

    /** 真实加密器（BCrypt 速度够快，不需要 mock） */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 真实 JwtUtil（用测试配置） */
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-for-unit-test-32bytes!"); // >= 32 字节
        props.setExpireHours(1L);
        jwtUtil = new JwtUtil(props);
        authService = new AuthServiceImpl(userMapper, passwordEncoder, jwtUtil);
    }

    // ========== 注册测试 ==========

    @Test
    void register_success() {
        // Arrange：用户名和手机号都不重复
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setPhone("13800001111");

        User inserted = new User();
        inserted.setId(10L);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L); // 模拟数据库自增回填
            return 1;
        });

        // Act
        Long id = authService.register(dto);

        // Assert
        assertEquals(10L, id, "返回新用户 id");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_duplicateUsername() {
        // Arrange：用户名已存在（selectCount 返回 1）
        when(userMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(1L) // 第一次查用户名：已存在
                .thenReturn(0L); // 手机号不走（因为用户名先报错）

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("existinguser");
        dto.setPassword("123456");
        dto.setPhone("13800001111");

        // Act & Assert：用户名重复应抛业务异常
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(dto));
        assertTrue(ex.getMessage().contains("用户名"));
    }

    @Test
    void register_duplicatePhone() {
        // Arrange：用户名不重复，但手机号重复
        when(userMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L) // 查用户名：不存在
                .thenReturn(1L); // 查手机号：已存在

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("uniqueuser");
        dto.setPassword("123456");
        dto.setPhone("13800009999");

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(dto));
        assertTrue(ex.getMessage().contains("手机号"));
    }

    // ========== 登录测试 ==========

    @Test
    void login_success() {
        // Arrange：查到用户，密码匹配
        User user = new User();
        user.setId(1L);
        user.setUsername("buyer001");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setPhone("1380000100");
        user.setRole(0);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        LoginDTO dto = new LoginDTO();
        dto.setAccount("buyer001");
        dto.setPassword("123456");

        // Act
        LoginVO result = authService.login(dto);

        // Assert
        assertNotNull(result.getToken(), "应返回 token");
        assertEquals("buyer001", result.getUsername());
        assertEquals(0, result.getRole());
    }

    @Test
    void login_wrongPassword() {
        // Arrange：查到用户，但密码不匹配
        User user = new User();
        user.setId(1L);
        user.setUsername("buyer001");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setPhone("1380000100");
        user.setRole(0);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        LoginDTO dto = new LoginDTO();
        dto.setAccount("buyer001");
        dto.setPassword("wrongpassword");

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> authService.login(dto));
    }

    @Test
    void login_nonExistentAccount() {
        // Arrange：查不到用户
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        LoginDTO dto = new LoginDTO();
        dto.setAccount("nobody");
        dto.setPassword("123456");

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> authService.login(dto));
    }
}
