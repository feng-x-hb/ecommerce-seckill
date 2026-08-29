package com.example.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @MapperScan：告诉 MyBatis-Plus 去 com.example.mall.mapper 包下扫描所有 Mapper 接口，
 * 自动注册成 Spring Bean。这样 ProductMapper、SkuMapper 等新建的 Mapper 就不需要额外配置。
 */
@SpringBootApplication
@MapperScan("com.example.mall.mapper")
public class MallApplication {

	public static void main(String[] args) {
		SpringApplication.run(MallApplication.class, args);
	}

}
