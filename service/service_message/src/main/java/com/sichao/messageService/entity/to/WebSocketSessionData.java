package com.sichao.messageService.entity.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: sjc
 * @createTime: 2023年05月24日 15:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketSessionData implements Serializable, WebSocketSession {
    private String id;
    private URI uri;
    private Map<String, Object> attributes;
    private Instant lastActiveTime;

    @Override
    public HttpHeaders getHandshakeHeaders() {
        return null;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public String getAcceptedProtocol() {
        return null;
    }

    @Override
    public void setTextMessageSizeLimit(int messageSizeLimit) {

    }

    @Override
    public int getTextMessageSizeLimit() {
        return 0;
    }

    @Override
    public void setBinaryMessageSizeLimit(int messageSizeLimit) {

    }

    @Override
    public int getBinaryMessageSizeLimit() {
        return 0;
    }

    @Override
    public List<WebSocketExtension> getExtensions() {
        return null;
    }

    @Override
    public void sendMessage(WebSocketMessage<?> message) throws IOException {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void close(CloseStatus status) throws IOException {

    }
}
