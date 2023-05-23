package com.sichao.messageService.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.messageService.entity.ChatUserLink;
import com.sichao.messageService.entity.vo.ChatListVo;
import com.sichao.messageService.mapper.ChatUserLinkMapper;
import com.sichao.messageService.service.ChatUserLinkService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户聊天关系表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-05-23
 */
@Service
public class ChatUserLinkServiceImpl extends ServiceImpl<ChatUserLinkMapper, ChatUserLink> implements ChatUserLinkService {
    // 根据当前用户为接收用户，从数据库中查找所有与当前用户相关的聊天列表
    @Override
    public List<ChatListVo> getCurrentUserChatList(String userId) {
        List<ChatListVo> chatListVoList = baseMapper.getCurrentUserChatList(userId);
        return chatListVoList;
    }
}
