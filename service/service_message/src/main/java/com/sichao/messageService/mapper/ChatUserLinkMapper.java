package com.sichao.messageService.mapper;

import com.sichao.messageService.entity.ChatUserLink;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sichao.messageService.entity.vo.ChatListVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 用户聊天关系表 Mapper 接口
 * </p>
 *
 * @author jicong
 * @since 2023-05-23
 */
@Mapper
public interface ChatUserLinkMapper extends BaseMapper<ChatUserLink> {
    // 根据当前用户为接收用户，从数据库中查找所有与当前用户相关的聊天列表
    List<ChatListVo> getCurrentUserChatList(String userId);
    //查询聊天列表项(当前用户时接收方)
    ChatListVo getChatListItem(String fromUserId, String toUserId);
}
