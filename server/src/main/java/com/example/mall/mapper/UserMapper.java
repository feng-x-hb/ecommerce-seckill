package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层（Mapper）
 * 继承 BaseMapper<User> 后自动获得 user 表的增删改查方法
 * 无需手写 SQL，MyBatis-Plus 根据 User 实体自动生成
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}