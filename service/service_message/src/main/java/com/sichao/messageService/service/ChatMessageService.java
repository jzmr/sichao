package com.sichao.messageService.service;

import com.sichao.messageService.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.messageService.entity.vo.ChatMessageVo;

import java.util.List;

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
}
