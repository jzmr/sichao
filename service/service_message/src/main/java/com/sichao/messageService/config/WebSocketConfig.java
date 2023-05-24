package com.sichao.messageService.config;

import com.sichao.messageService.controller.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @Description: WebSocket配置类
 * @author: sjc
 * @createTime: 2023年05月23日 14:39
 *
 * 使用Webscoket请求时，项目的springMVC的拦截器不生效，所以要配置HandshakeInterceptor拦截器。
 * （Aop切面是会生效的）
 *
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebMvcConfigurer,WebSocketConfigurer {
    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myWebSocketHandler, "/messageService/ws")
                .setAllowedOrigins("*")
                .addInterceptors(new MyHandshakeInterceptor());
    }

    /**WebSocket拦截器：
     * 在Spring Boot中整合WebSocket时，如果WebSocket连接被打开，WebSocketSession将被创建，并且WebSocketHandler将处理所有
     * 消息传入WebSocketSession，这将导致普通的Spring MVC Interceptor无法在WebSocketHandler之前拦截WebSocket请求。这是因
     * 为Spring Boot框架提供了一个特殊的拦截器（HandshakeInterceptor），用于处理WebSocket握手请求。
     */
    private class MyHandshakeInterceptor implements HandshakeInterceptor {
        //在握手之前进行拦截
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//            System.out.println("Before Handshake");
            return true;
        }
        // 在握手之后进行拦截
        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
//            System.out.println("After Handshake");
        }
    }
}
