package com.sichao.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @Description:
 * @author: sjc
 * @createTime: 2023年05月03日 21:02
 */
@Configuration
public class RedissonConfig {
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.database}")
    private int database;
//    @Value("${spring.data.redis.password}")
//    private String password;//开发阶段没有设置redis密码

    @Bean(destroyMethod = "shutdown")
    RedissonClient redissonClient() {
        Config config = new Config();
        //Redis多节点
        // config.useClusterServers()
        //     .addNodeAddress("redis://127.0.0.1:6379", "redis://127.0.0.1:7001");
        //Redis单节点
        SingleServerConfig singleServerConfig = config.useSingleServer();
        //可以用"rediss://"来启用SSL连接
        String address = "redis://" + host + ":" + port;
        singleServerConfig.setAddress(address);
        //设置 数据库编号
        singleServerConfig.setDatabase(database);
        //singleServerConfig.setPassword(password);
        //连接池大小:默认值：64
        // singleServerConfig.setConnectionPoolSize()
        return Redisson.create(config);
    }
}
