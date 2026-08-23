package com.example.mall.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * 这里只做一件事：注册"字段自动填充器"。
 * 你的 user 表 created_at / updated_at 是 NOT NULL 且没有数据库默认值，
 * 所以每次插入都必须有值。有了这个填充器，只要实体字段标了
 * @TableField(fill = ...)，框架就会在插入/更新前自动把当前时间填进去，
 * 业务代码里完全不用手动 set 时间。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            /** 插入时填充 */
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
            }

            /** 更新时填充 */
            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
