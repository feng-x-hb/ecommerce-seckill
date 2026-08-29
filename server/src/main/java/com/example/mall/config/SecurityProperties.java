package com.example.mall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全相关配置（SecurityProperties）
 *
 * 它用来集中管理"哪些接口是公开的（不需要登录就能访问）"。
 * 数据来自 application.yaml 里的 mall.security.public-paths 列表。
 *
 * 为什么要做成"配置清单"而不是在拦截器里写死？
 *   因为公开接口会越来越多（分类列表、商品列表、商品详情……）。
 *   如果每加一个就去改 WebMvcConfig 的代码，既容易漏又难维护。
 *   现在只要往 yaml 的白名单里加一行路径即可，拦截器自动放行——这就是"白名单"思想。
 */
@Component
@ConfigurationProperties(prefix = "mall.security")
public class SecurityProperties {

    /** 公开路径清单（Ant 风格，支持 * 通配）。默认空，由 yaml 注入。 */
    private List<String> publicPaths = new ArrayList<>();

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }
}
