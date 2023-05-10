package com.sichao.blogService.listener;

import com.rabbitmq.client.Channel;
import com.sichao.blogService.service.BlogTopicRelationService;
import com.sichao.common.constant.RabbitMQConstant;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Description: 博客模块rabbitMQ消费者类
 * @author: sjc
 * @createTime: 2023年05月10日 15:44
 */
@Component
public class blogRabbitMQListener {
    @Autowired
    private BlogTopicRelationService blogTopicRelationService;


    //监听队列,博客绑定与话题关系
    @RabbitListener(queues = RabbitMQConstant.BLOG_BINDING_TOPIC_QUEUE)
    public void blogBindingTopic(Message message, Map<String,Object> topicMap, Channel channel){
        String blogId = (String) topicMap.get("blogId");
        List<String> topicIdList = (List<String>) topicMap.get("topicIdList");
        blogTopicRelationService.blogBindingTopicBatch(blogId,topicIdList);
    }


}
