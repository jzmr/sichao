package com.sichao.messageService.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.messageService.entity.ChatMessage;
import com.sichao.messageService.mapper.ChatMessageMapper;
import com.sichao.messageService.service.ChatMessageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天内容详情表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-05-23
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

}
