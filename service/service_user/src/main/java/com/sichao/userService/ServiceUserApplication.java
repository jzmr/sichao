package com.sichao.userService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @MapperScan 注解用于扫描 Mapper 接口，并将其实例化后交给 Spring 容器管理，使得可以
 * 在其他组件中使用这些 Mapper 接口的实现类。这样就不用在mapper接口类上使用@Mapper注解了
 */
@SpringBootApplication
@EnableDiscoveryClient//nacos注册
@EnableFeignClients//开启feign调用（在调用端使用）
@ComponentScan("com.sichao")//指定扫描路径（这个注解去扫描配置类）
@MapperScan("com.sichao.*.mapper")//指定扫描mapper类(使用此配置‘*’就可以扫描到common模块下关于任务执行信息表的mapper)
public class ServiceUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApplication.class, args);
    }

}
