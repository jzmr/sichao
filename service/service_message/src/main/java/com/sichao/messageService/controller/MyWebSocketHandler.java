package com.sichao.messageService.controller;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import com.sichao.messageService.entity.to.User;
import com.sichao.messageService.entity.vo.ChatListVo;
import com.sichao.messageService.service.ChatUserLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.OnOpen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description: WebSocketHandler处理所有WebSocket消息
 * @author: sjc
 * @createTime: 2023年05月23日 14:41
 *
 * WebSocket会话在服务节点之间不会共享，因此无法跨服务节点广播消息。
 * 当使用消息队列或者Redis等作为后台代理时，必须处理并发问题，避免同一WebSocket连接的多个线程在不同实例上处理相同的消息。
 *
 * 由于 WebSocket 会话需要在多个服务节点间共享，所以需要使用一个可以在不同服务节点之间共享的存储来保存 WebSocket 会话信息。
 * 常见的共享存储包括 Redis、ZooKeeper 等。
 * 假设使用 Redis 来保存 WebSocket 会话信息，可以将会话信息保存在 Redis 中，将用户Id作为键，会话信息作为值。在服务端接
 * 收到 WebSocket 连接请求时，根据ThreadLocal中保存的用户id，从 Redis 中获取对应的会话信息，建立 WebSocket 连接并处理
 * 消息。
 * 以上是一种解决方案，实现起来复杂度较高，需要综合考虑分布式环境下的负载均衡、高可用性等问题。因此，可以根据具体应用场景和
 * 需求，选择合适的解决方案。
 */
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ChatUserLinkService chatUserLinkService;



    //连接成功之后，接收到消息调用的方法
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("=========接收到消息调用的方法");
    }

    //连接建立后回调
    //@Override
    @OnOpen
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("=========连接建立后回调");
    }
    //连接关闭后回调
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("=========连接关闭后回调");
    }
}
