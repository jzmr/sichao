package com.sichao.messageService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient//nacos注册
@EnableFeignClients//开启feign调用（在调用端使用）
//@EnableRabbit//开启RabbitMQ的使用
@ComponentScan("com.sichao")//指定扫描路径（这个注解去扫描配置类）
@MapperScan("com.sichao.*.mapper")//指定扫描mapper类(使用此配置‘*’就可以扫描到common模块下关于任务执行信息表的mapper)
public class ServiceMessageApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceMessageApplication.class, args);
	}

}
