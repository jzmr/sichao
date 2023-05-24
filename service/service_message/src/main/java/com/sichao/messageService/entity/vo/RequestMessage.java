package com.sichao.messageService.entity.vo;

import lombok.Data;

/**
 * @Description: websocket消息传输入服务器规定格式
 * @author: sjc
 * @createTime: 2023年05月24日 12:14
 */
@Data
public class RequestMessage {
    private String tag;//标识:用来标识该消息的作用(比如：查询聊天列表、加载聊天记录.....)
    private String targetUserId;//目标用户(接受用户)id
    private String content;//消息内容
    private String username;
}