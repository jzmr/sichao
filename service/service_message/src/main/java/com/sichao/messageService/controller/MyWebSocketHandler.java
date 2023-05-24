package com.sichao.messageService.controller;

import com.alibaba.fastjson2.JSON;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.utils.JwtUtils;
import com.sichao.common.utils.R;
import com.sichao.messageService.entity.vo.ChatListVo;
import com.sichao.messageService.entity.vo.ChatMessageVo;
import com.sichao.messageService.entity.vo.RequestMessage;
import com.sichao.messageService.service.ChatMessageService;
import com.sichao.messageService.service.ChatUserLinkService;
import jakarta.websocket.OnOpen;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private ChatUserLinkService chatUserLinkService;
    @Autowired
    private ChatMessageService chatMessageService;




    //连接成功之后，接收到消息调用的方法
    //message:TextMessage payload=[1231231231..], byteCount=17, last=true]
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("=========接收到消息调用的方法");
        // 1. 解析请求的内容
        String payload = message.getPayload();//获取有效荷载
        RequestMessage requestMessage = JSON.parseObject(payload, RequestMessage.class);

        String messageContent = requestMessage.getTag();//获取标识
        String currentUserId = getUserIdBySessionToken(session);//当前用户id
        if(currentUserId == null)return;
        String targetUserId = requestMessage.getTargetUserId();//目标用户id

        // 2. 判断消息内容，去执行对应的方法
        if("loadMessage".equals(messageContent)){//加载聊天记录
            List<ChatMessageVo> chatMessageList = chatMessageService.loadMessage(currentUserId,targetUserId);

            R r = R.ok().message("loadMessage").data("chatMessageList",chatMessageList);
            session.sendMessage(new TextMessage(JSON.toJSONBytes(r)));//给指定session的websocket的客户端发送响应消息
        }


    }

    //连接建立后回调
//    @Override
    @OnOpen
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("=========连接建立后回调");

        //获取请求地址中的参数token,而后根据token获取用户信息
        String userId = getUserIdBySessionToken(session);
        if(userId==null)return;

        //使用redis的hash类型，已WebSocket会话id为key，已new一个sessionMap保存id与uri为value（WebSocketSession不能序列化，不能直接保存到redis中使用要有一个中转）
        HashOperations<String, String, Object> forHash = redisTemplate.opsForHash();
        String messageWebsocketKey = PrefixKeyConstant.MESSAGE_WEBSOCKET_PREFIX+userId;//指定用户id的webSocket会话

        //首先判断当前用户是否已经登录, 防止用户多开
        if(forHash.get(messageWebsocketKey,"id") != null) {//redis中以存在该会话缓存
            R r = R.error().message("当前用户已经登录了, 不要重复登录");
            session.sendMessage(new TextMessage(JSON.toJSONBytes(r)));//给指定session的websocket的客户端发送响应消息
            return;
        }
        //将session保存带redis中，表示用户的状态为在线(这里的在线指的是webscoker连接，而不是用户是否使用本系统)
        //session:StandardWebSocketSession[id=b3dc5ac6-df31-ea71-2968-0698378bd58c, uri=ws://192.168.78.1:11000/messageService/ws]
        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("id", session.getId());
        sessionMap.put("uri", session.getUri().toString());
        forHash.putAll(messageWebsocketKey, sessionMap);//通过会话id与url就能还原出WebSocketSession
        //根据当前用户为接收用户，从数据库中查找所有与当前用户相关的聊天列表
        List<ChatListVo> chatList = chatUserLinkService.getCurrentUserChatList(userId);
        //设置响应类, 并添加对应的信息
        R r = R.ok().message("getChatList").data("chatList",chatList);
        //返回响应
        session.sendMessage(new TextMessage(JSON.toJSONBytes(r)));//给指定session的websocket的客户端发送响应消息
    }
    //连接关闭后回调
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("=========连接关闭后回调");
        String userId = getUserIdBySessionToken(session);
        if(userId==null)return;
        String messageWebsocketKey = PrefixKeyConstant.MESSAGE_WEBSOCKET_PREFIX+userId;//指定用户id的webSocket会话
        redisTemplate.delete(messageWebsocketKey);
    }

    //获取WebSocketSession中uri里面的token，然后根据token获取用户id
    public String getUserIdBySessionToken(WebSocketSession session) throws Exception {
        //1、 获取请求地址中的参数token
        String query = session.getUri().getQuery();
        String jwtToken = query.split("=")[1];
        if (!StringUtils.hasText(jwtToken) || !JwtUtils.checkToken(jwtToken)) {//判断token不存在或token失效时
            R r = R.error().message("未登录");
            session.sendMessage(new TextMessage(JSON.toJSONBytes(r)));//给指定session的websocket的客户端发送响应消息
            return null;
        }
        //2、根据token获取用户信息
        return JwtUtils.getUserIdByJwtToken(jwtToken);
    }
}
