package com.sichao.messageService.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.messageService.entity.ChatList;
import com.sichao.messageService.mapper.ChatListMapper;
import com.sichao.messageService.service.ChatListService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天列表表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-05-23
 */
@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatList> implements ChatListService {

}
