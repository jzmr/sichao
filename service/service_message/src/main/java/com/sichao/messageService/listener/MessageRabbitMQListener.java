package com.sichao.messageService.listener;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import com.sichao.common.constant.RabbitMQConstant;
import com.sichao.common.entity.MqMessage;
import com.sichao.common.mapper.MqMessageMapper;
import com.sichao.common.utils.R;
import com.sichao.messageService.utils.ChatOnlineUserManager;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

/**
 * @Description:
 * @author: sjc
 * @createTime: 2023年05月25日 22:58
 */
@Component
public class MessageRabbitMQListener {
    @Autowired
    private MqMessageMapper mqMessageMapper;
    @Autowired
    private ChatOnlineUserManager chatOnlineUserManager;

    //监听队列,将userId与message发送到队列中广播给使用订阅的消费者，该消费者中存放session的map集合有与userId一样的key时，向该session发送message）
    //session在所有消费者中只在一个消费者中存在一份
    @RabbitListener(queues = RabbitMQConstant.MESSAGE_SEND_QUEUE)
    public void blogBindingTopic(Message message, Map<String,Object> map, Channel channel) throws IOException {
        String userId = (String) map.get("userId");
        String jsonString = (String) map.get("jsonString");
        R r = JSON.parseObject(jsonString, R.class);

        //该消费者存在该用户的WebSocketSession
        if(chatOnlineUserManager.getState(userId)!=null){
            WebSocketSession session = chatOnlineUserManager.getState(userId);
            session.sendMessage(new TextMessage(JSON.toJSONBytes(r)));//给指定session的websocket的客户端发送响应消息
        }

        //能执行到这里了说明消息已经被消费，将消费信息持久化到MQ消息表
        //获取携带的数据标识
        String mqMessageId = (String) message.getMessageProperties().getHeaders().get("spring_returned_message_correlation");
        MqMessage mqMessage = new MqMessage();
        mqMessage.setId(mqMessageId);
        mqMessage.setStatus((byte)3);//消息已被消费状态码
        mqMessageMapper.updateById(mqMessage);

        //如果这里之前发生异常，则之后不会签收货物，信息会被重新消费，所以消费数据时要做幂等性处理
        //没有收到ACK消息，消费者断开连接后，RabbitMQ会把这条消息发送给其他消费者。
        //如果没有其他消费者，消费者重启后会重新消费这条消息，重复执行业务逻辑。（消费者代码要有幂等性处理）

        //投递标签：channel内按顺序自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //手动确认签收货物，非批量模式
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
