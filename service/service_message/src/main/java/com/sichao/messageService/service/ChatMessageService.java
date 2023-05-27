package com.sichao.messageService.service;

import com.sichao.common.utils.R;
import com.sichao.messageService.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.messageService.entity.vo.ChatMessageVo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 聊天内容详情表 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-05-23
 */
public interface ChatMessageService extends IService<ChatMessage> {
    //加载聊天记录
    List<ChatMessageVo> loadMessage(String currentUserId, String targetUserId);
    //当前用户发送消息给目标用户
    ChatMessageVo sendChatMessage(String currentUserId, String targetUserId, String content);
    //向指定用户id的websocket会话发送消息
    void sendMessageByUserId(String userId, R r);
    //博客@用户处理
    void blogAtUserHandele(String blogId, String blogContent,String blogCreatorId,String blogCreatorNickname, List<String> userIdList);
    //评论@用户处理
    void commentAtUserHandele(String blogCommentId, String blogId, String commentContent, String commentCreatorId, String commentCreatorNickname, List<String> userIdList);
}
