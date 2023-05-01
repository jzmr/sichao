package com.sichao.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

//redis配置类
@Configuration//声明为配置类
@EnableCaching//开启缓存注解
public class RedisConfig extends CachingConfigurerSupport {
    //以下是配置了两个插件
    /**
     * 自定义RedisTemplate的序列化方式：      (StringRedisTemplate不需要序列化)
     * RedisTemplate可以接收任意Object作为值写入Redis，只不过写入前会把Object序列化为字节形式，
     * 默认是采用JDK序列化缺点：可读性差、内存占用较大，可以通过自定义RedisTemplate的序列化方式来解决
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建RedisTemplate对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        //设置String序列化器，key使用字符串
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        //设置Jackson序列化器，value使用Object
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 设置连接工厂
        template.setConnectionFactory(factory);
        // 设置Key的序列化
        template.setKeySerializer(redisSerializer);
        template.setHashKeySerializer(redisSerializer);//hashmap序列化
        // 设置Value的序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);//hashmap序列化

        return template;
    }

    //默认缓存管理器初始化配置
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        //1.序列化（一般用于key值）
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        //2.引入json串的转化类（一般用于value的处理）
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        //2.1设置objectMapper的访问权限
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //2.2指定序列化输入类型,就是将数据库里的数据按照一定类型存储到redis缓存中。
        //om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);//过期，使用下面这一句代替
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //3.序列话配置，乱码问题解决以及我们缓存的时效性 缓存过期时间 600s
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(600))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues();//空值不存入缓存
        //4.创建cacheManager链接并设置属性
        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
        return cacheManager;
    }
}
