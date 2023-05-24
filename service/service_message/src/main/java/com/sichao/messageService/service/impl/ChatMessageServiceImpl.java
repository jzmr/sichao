package com.sichao.messageService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.messageService.entity.ChatMessage;
import com.sichao.messageService.entity.ChatUserLink;
import com.sichao.messageService.entity.vo.ChatMessageVo;
import com.sichao.messageService.mapper.ChatMessageMapper;
import com.sichao.messageService.mapper.ChatUserLinkMapper;
import com.sichao.messageService.service.ChatMessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    private ChatUserLinkMapper chatUserLinkMapper;

    //加载聊天记录
    @Override
    public List<ChatMessageVo> loadMessage(String currentUserId, String targetUserId) {
        //查询linkId
        QueryWrapper<ChatUserLink> chatUserLinkWrapper = new QueryWrapper<>();
        chatUserLinkWrapper.eq("from_user_id",currentUserId)
                .eq("to_user_id",targetUserId)
                .or(w -> w.eq("from_user_id",targetUserId)
                        .eq("to_user_id",currentUserId)
                );
        ChatUserLink chatUserLink = chatUserLinkMapper.selectOne(chatUserLinkWrapper);
        String linkId = chatUserLink.getId();

        //根据linkId获取聊天记录
        QueryWrapper<ChatMessage> chatMessageWrapper = new QueryWrapper<>();
        chatMessageWrapper.eq("link_id",linkId)
                .orderByDesc("create_time")
                .last("limit 100")
                .select("id","from_user_id","content","create_time");
        List<ChatMessage> chatMessageList = baseMapper.selectList(chatMessageWrapper);

        List<ChatMessageVo> chatMessagesVoList = new ArrayList<>();
        for (ChatMessage message : chatMessageList) {
            ChatMessageVo chatMessageVo = new ChatMessageVo();
            BeanUtils.copyProperties(message, chatMessageVo);
            chatMessagesVoList.add(0,chatMessageVo);//倒插
        }
        return chatMessagesVoList;
    }
}
