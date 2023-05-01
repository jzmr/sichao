package com.sichao.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: mybatis-plus配置类
 * @author: sjc
 * @createTime: 2023年04月29日 16:02
 */
@Configuration
public class MybatisPlusConfig {

    //MybatisPlus拦截器
    //多个插件使用的情况，请将分页插件放到'插件执行链'最后面。比如在租户插件前面，会出现 COUNT 执行 SQL 不准确问题。
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //配置乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        //防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        //新的分页插件,一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false
        // 避免缓存出现问题(该属性会在旧插件移除后一同移除)
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));//指定是mysql数据库
        return interceptor;
    }

    /**
     * 关于分页插件的使用
     *      Page<User> page = new Page<>(1,5);//指定想要查询的页数与每页数据条数
     *      userMapper.selectPage(page, null);//执行查询
     */

}
