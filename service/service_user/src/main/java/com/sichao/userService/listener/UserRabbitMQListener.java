package com.sichao.userService.listener;

import com.rabbitmq.client.Channel;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.constant.RabbitMQConstant;
import com.sichao.common.entity.MqMessage;
import com.sichao.common.mapper.MqMessageMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description: 用户模块rabbitMQ消费者类
 * @author: sjc
 * @createTime: 2023年05月10日 16:29
 */
@Component
public class UserRabbitMQListener {
    @Autowired
    private MqMessageMapper mqMessageMapper;

    //监听队列,博客@用户
    @RabbitListener(queues = RabbitMQConstant.BLOG_AT_USER_QUEUE)
    public void blogAtUser(Message message, Map<String,Object> map, Channel channel){
        String blogId = (String) map.get("blogId");
        List<String> userIdList = (List<String>) map.get("userIdList");
        // TODO 私信模块处理私信
        System.out.println("========用户模块RabbitMQ消费消息"+userIdList);


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

    //监听队列,评论@用户
    @RabbitListener(queues = RabbitMQConstant.BLOG_COMMENT_AT_USER_QUEUE)
    public void commentAtUser(Message message, Map<String,Object> map, Channel channel){
        String blogCommentId = (String) map.get("blogCommentId");
        List<String> userIdList = (List<String>) map.get("userIdList");
        // TODO 私信模块处理私信
        System.out.println("========用户模块RabbitMQ消费消息"+userIdList);


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