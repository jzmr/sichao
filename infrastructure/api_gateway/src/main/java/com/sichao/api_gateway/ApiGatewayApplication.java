package com.sichao.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//网关模块（config、filter和handler这三个文件夹一起完成了跨域问题，这样的话经过这个网关的服务就不用在controller方法上加@CrossOrigin注解用来解决跨域）
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//默认不去加载数据库配置
@EnableDiscoveryClient//nacos注册
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
