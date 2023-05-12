package com.sichao.service_oss;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication//这里因为引入的common模块中，有mapper类，所以这里不能使用下面的不加载数据库配置的写法
//@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//默认不去加载数据库配置
@EnableDiscoveryClient//nacos注册
@EnableFeignClients//开启feign调用（在调用端使用）
@ComponentScan("com.sichao")//指定扫描路径
@MapperScan("com.sichao.*.mapper")//指定扫描mapper类(使用此配置‘*’就可以扫描到common模块下关于任务执行信息表的mapper)
public class ServiceOssApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceOssApplication.class, args);
    }

}
