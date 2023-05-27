package com.sichao.messageService.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.RabbitMQConstant;
import com.sichao.common.entity.MqMessage;
import com.sichao.common.mapper.MqMessageMapper;
import com.sichao.common.utils.R;
import com.sichao.messageService.entity.ChatList;
import com.sichao.messageService.entity.ChatMessage;
import com.sichao.messageService.entity.ChatUserLink;
import com.sichao.messageService.entity.vo.ChatMessageVo;
import com.sichao.messageService.mapper.ChatListMapper;
import com.sichao.messageService.mapper.ChatMessageMapper;
import com.sichao.messageService.mapper.ChatUserLinkMapper;
import com.sichao.messageService.service.ChatMessageService;
import com.sichao.messageService.utils.ChatOnlineUserManager;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ChatOnlineUserManager chatOnlineUserManager;
    @Autowired
    private MqMessageMapper mqMessageMapper;
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

    //向指定用户id的websocket会话发送消息
    @Override
    public void sendMessageByUserId(String userId,R r){
        if (chatOnlineUserManager.getState(userId)!=null){//当前服务器（消费者）中存在该session
            WebSocketSession targetSession = chatOnlineUserManager.getState(userId);
            try {
                targetSession.sendMessage(new TextMessage(JSON.toJSONBytes(r)));//给指定session的websocket的客户端发送响应消息
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {//不存在该session，则发送rabbitMq广播所有订阅中的队列的消费者，寻找session并处理
            String jsonString = JSON.toJSONString(r);
            byte[] bytesR = JSON.toJSONBytes(r);
            Map<String,Object> rabbitMqMap = new HashMap<>();
            rabbitMqMap.put("userId",userId);
            rabbitMqMap.put("jsonString",jsonString);
            //发送消息前先记录数据
            String topicMapJson = JSON.toJSONString(rabbitMqMap);
            MqMessage mqMessage = new MqMessage(topicMapJson, RabbitMQConstant.MESSAGE_EXCHANGE,RabbitMQConstant.MESSAGE_SEND_ROUTINGKEY,
                    "Map<String,Object>",(byte)0);
            mqMessageMapper.insert(mqMessage);

            //指定路由，给交换机发送数据，并且携带数据标识
            rabbitTemplate.convertAndSend(RabbitMQConstant.MESSAGE_EXCHANGE,RabbitMQConstant.MESSAGE_SEND_ROUTINGKEY,
                    rabbitMqMap,new CorrelationData(mqMessage.getId()));//以mq消息表id作为数据标识
        }
    }

    //博客@用户处理
    @Transactional
    @Override
    public void blogAtUserHandele(String blogId, String blogContent,String blogCreatorId,String blogCreatorNickname, List<String> userIdList) {
        for (String userId : userIdList) {
            StringBuilder strb=new StringBuilder();//用来拼接内容   （用户<a>用户id</a>在<a>博客<a/>(博客内容)中@了你）
            strb.append("用户"+Constant.BLOG_AT_USER_HYPERLINK_PREFIX+blogCreatorId+Constant.BLOG_AT_USER_HYPERLINK_INFIX+blogCreatorNickname+Constant.BLOG_AT_USER_HYPERLINK_SUFFIX)
                    .append("在"+Constant.BLOG_DETAIL_HYPERLINK_PREFIX+blogId+Constant.BLOG_DETAIL_HYPERLINK_INFIX+"博客"+Constant.BLOG_DETAIL_HYPERLINK_SUFFIX+"("+blogContent+")")
                    .append("中@了你。");
            ChatMessageVo chatMessageVo = sendChatMessage(Constant.BLOG_AT_USER_OFFICIAL_USER_ID,userId,strb.toString());//发送@消息

            //向指定用户id的websocket会话发送消息
            R r = R.ok().message("sendChatMessage").data("chatMessageVo",chatMessageVo);
            sendMessageByUserId(userId,r);
        }
    }

    //博客@用户处理
    @Override
    public void commentAtUserHandele(String blogCommentId, String blogId, String commentContent, String commentCreatorId, String commentCreatorNickname, List<String> userIdList) {
        for (String userId : userIdList) {
            StringBuilder strb=new StringBuilder();//用来拼接内容   （用户<a>用户id</a>在<a>博客<a/>下的评论(评论内容)中@了你）

            strb.append("用户"+Constant.BLOG_AT_USER_HYPERLINK_PREFIX+commentCreatorId+Constant.BLOG_AT_USER_HYPERLINK_INFIX+commentCreatorNickname+Constant.BLOG_AT_USER_HYPERLINK_SUFFIX)
                    .append("在"+Constant.BLOG_DETAIL_HYPERLINK_PREFIX+blogId+Constant.BLOG_DETAIL_HYPERLINK_INFIX+"博客"+Constant.BLOG_DETAIL_HYPERLINK_SUFFIX+"下的评论("+commentContent+")")
                    .append("中@了你。");
            ChatMessageVo chatMessageVo = sendChatMessage(Constant.BLOG_AT_USER_OFFICIAL_USER_ID,userId,strb.toString());//发送@消息

            //向指定用户id的websocket会话发送消息
            R r = R.ok().message("sendChatMessage").data("chatMessageVo",chatMessageVo);
            sendMessageByUserId(userId,r);
        }
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
