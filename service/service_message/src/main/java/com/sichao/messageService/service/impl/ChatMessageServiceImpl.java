package com.sichao.messageService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.messageService.entity.ChatList;
import com.sichao.messageService.entity.ChatMessage;
import com.sichao.messageService.entity.ChatUserLink;
import com.sichao.messageService.entity.vo.ChatMessageVo;
import com.sichao.messageService.mapper.ChatListMapper;
import com.sichao.messageService.mapper.ChatMessageMapper;
import com.sichao.messageService.mapper.ChatUserLinkMapper;
import com.sichao.messageService.service.ChatMessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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
    @Autowired
    private ChatListMapper chatListMapper;

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

        if(chatUserLink == null) {
            return null;
        }
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

    //当前用户发送消息给目标用户
    @Transactional
    @Override
    public ChatMessageVo sendChatMessage(String currentUserId, String targetUserId, String content) {
        //查询linkId
        QueryWrapper<ChatUserLink> chatUserLinkWrapper = new QueryWrapper<>();
        chatUserLinkWrapper.eq("from_user_id",currentUserId)
                .eq("to_user_id",targetUserId)
                .or(w -> w.eq("from_user_id",targetUserId)
                        .eq("to_user_id",currentUserId)
                );
        ChatUserLink chatUserLink = chatUserLinkMapper.selectOne(chatUserLinkWrapper);

        String linkId = null;
        //判断当前linkId是否为空, 为空需要创建linkId
        if(chatUserLink == null) {
            ChatUserLink link = new ChatUserLink();
            link.setFromUserId(currentUserId);
            link.setToUserId(targetUserId);
            chatUserLinkMapper.insert(link);
            linkId = link.getId();//回显获取linkId

            //保存聊天列表信息
            saveChatList(linkId,currentUserId,targetUserId);
        }else {
            linkId = chatUserLink.getId();
        }


        //保存消息
        ChatMessage chatMessage = new ChatMessage(linkId,currentUserId,targetUserId,content);
        baseMapper.insert(chatMessage);

        //将该消息封装成消息vo对象并返回
        ChatMessageVo chatMessageVo = new ChatMessageVo(chatMessage.getId(),currentUserId,targetUserId,content,chatMessage.getCreateTime());
        return chatMessageVo;
    }

    //保存聊天列表信息
    public void saveChatList(String linkId,String currentUserId,String targetUserId){
        ChatList chatList1 = new ChatList();
        chatList1.setLinkId(linkId);
        chatList1.setFromUserId(currentUserId);
        chatList1.setToUserId(targetUserId);
        chatListMapper.insert(chatList1);

        ChatList chatList2 = new ChatList();
        chatList2.setLinkId(linkId);
        chatList2.setFromUserId(targetUserId);
        chatList2.setToUserId(currentUserId);
        chatListMapper.insert(chatList2);
    }
}
