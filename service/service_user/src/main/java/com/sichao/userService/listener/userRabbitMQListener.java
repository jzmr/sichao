package com.sichao.userService.listener;

import com.rabbitmq.client.Channel;
import com.sichao.common.constant.RabbitMQConstant;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Description: 用户模块rabbitMQ消费者类
 * @author: sjc
 * @createTime: 2023年05月10日 16:29
 */
@Component
public class userRabbitMQListener {

    //监听队列,博客@用户
    @RabbitListener(queues = RabbitMQConstant.BLOG_AT_USER_QUEUE)
    public void blogAtUser(Message message, Map<String,Object> topicMap, Channel channel){
        String blogId = (String) topicMap.get("blogId");
        List<String> userIdList = (List<String>) topicMap.get("userIdList");
        // TODO 私信模块处理私信
        System.out.println("========用户模块RabbitMQ消费消息"+userIdList);
    }


}
