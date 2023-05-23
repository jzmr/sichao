package com.sichao.messageService.config;

import com.sichao.messageService.controller.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @Description: WebSocket配置类
 * @author: sjc
 * @createTime: 2023年05月23日 14:39
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebMvcConfigurer,WebSocketConfigurer {
    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myWebSocketHandler, "/messageService/ws").setAllowedOrigins("*");
    }
}
