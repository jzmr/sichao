package com.sichao.messageService.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 聊天用户在线管理类，使用一个ConcurrentHashMap，以用户id为key，WebSocketSession为value保存会话
 * @author: sjc
 * @createTime: 2023年05月25日 22:26
 */
@Component
public class ChatOnlineUserManager {
    // 哈希表存储的是用户的当前的状态,在线就存储到哈希表中
    private ConcurrentHashMap<String, WebSocketSession> userState = new ConcurrentHashMap<>();

    public void enterHall(String userId, WebSocketSession webSocketSession) {
        userState.put(userId,webSocketSession);
    }

    public void exitHall(String userId) {
        userState.remove(userId);
    }

    public WebSocketSession getState(String userId) {
        return userState.get(userId);
    }

    public int getOnlinePeople() {
        return userState.size();
    }

    public Collection<WebSocketSession> getAllSession() {
        return userState.values();
    }
}
