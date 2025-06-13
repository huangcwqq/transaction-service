package com.hsbc.transaction.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类。
 * 定义并配置应用程序中使用的缓存管理器和缓存策略。
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置 Caffeine 缓存管理器。
     * 定义了1个缓存区域：
     * - "transactions": 用于缓存单个交易对象，例如通过 ID 查询的结果。
     *
     * @return 配置好的 CacheManager 实例
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 配置 "transactions" 缓存：
        // 最大缓存 1000 条记录
        // 写入后 10 分钟过期
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES));

        // 指定要管理的缓存名称，Spring 会根据这些名称查找对应的缓存
        cacheManager.setCacheNames(java.util.Set.of("transactions"));

        return cacheManager;
    }
}
