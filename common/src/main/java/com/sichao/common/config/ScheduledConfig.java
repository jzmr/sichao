package com.sichao.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Description: 定时任务配置类
 * @author: sjc
 * @createTime: 2023年05月05日 11:02
 */
@Configuration
@EnableScheduling//开启定时任务
@EnableAsync//开启异步任务
public class ScheduledConfig {
}
