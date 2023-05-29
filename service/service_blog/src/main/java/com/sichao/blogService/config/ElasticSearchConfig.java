package com.sichao.blogService.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: ElasticSearch配置类
 * @author: sjc
 * @createTime: 2023年05月28日 14:27
 */
@Configuration
public class ElasticSearchConfig {
    @Value("${elasticsearch.host}")
    private String host;
    @Value("${elasticsearch.port}")
    private int port;

    //配置client客户端
    @Bean
    public RestHighLevelClient esRestClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(host, port, "http")));
        return client;
    }

    //请求设置项（单例）
    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS=builder.build();
    }
}
