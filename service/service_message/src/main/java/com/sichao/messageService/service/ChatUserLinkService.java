package com.sichao.messageService.service;

import com.sichao.messageService.entity.ChatUserLink;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.messageService.entity.vo.ChatListVo;

import java.util.List;

/**
 * <p>
 * 用户聊天关系表 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-05-23
 */
public interface ChatUserLinkService extends IService<ChatUserLink> {

    // 根据当前用户为接收用户，从数据库中查找所有与当前用户相关的聊天列表
    List<ChatListVo> getCurrentUserChatList(String userId);
    //查询聊天列表项(当前用户时接收方)
    ChatListVo getChatListItem(String currentUserId, String targetUserId);
}
