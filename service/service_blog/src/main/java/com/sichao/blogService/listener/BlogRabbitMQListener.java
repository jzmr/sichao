package com.sichao.blogService.listener;

import com.rabbitmq.client.Channel;
import com.sichao.blogService.client.UserClient;
import com.sichao.blogService.service.BlogCommentService;
import com.sichao.blogService.service.BlogLikeUserService;
import com.sichao.blogService.service.BlogTopicRelationService;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.constant.RabbitMQConstant;
import com.sichao.common.entity.MqMessage;
import com.sichao.common.mapper.MqMessageMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description: 博客模块rabbitMQ消费者类
 * @author: sjc
 * @createTime: 2023年05月10日 15:44
 */
@Component
public class BlogRabbitMQListener {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserClient userClient;
    @Autowired
    private MqMessageMapper mqMessageMapper;
    @Autowired
    private BlogTopicRelationService blogTopicRelationService;

    @Autowired
    private BlogCommentService blogCommentService;
    @Autowired
    private BlogLikeUserService blogLikeUserService;


    //监听队列,博客绑定与话题关系
    @RabbitListener(queues = RabbitMQConstant.BLOG_BINDING_TOPIC_QUEUE)
    public void blogBindingTopic(Message message, Map<String,Object> map, Channel channel){

        String blogId = (String) map.get("blogId");
        List<String> topicIdList = (List<String>) map.get("topicIdList");
        blogTopicRelationService.blogBindingTopicBatch(blogId, topicIdList);

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

    //监听队列,删除博客
    @RabbitListener(queues = RabbitMQConstant.BLOG_DELETE_QUEUE)
    public void blogDelete(Message message, Map<String,Object> Map, Channel channel){
        String userId = (String) Map.get("userId");
        String blogId = (String) Map.get("blogId");

        blgoDeleteHandle(userId,blogId);//删除博客处理


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

    //删除博客处理方法
    @Transactional//事务管理
    public void blgoDeleteHandle(String userId,String blogId){
        //删除评论(批量)
        blogCommentService.deleteCommentBatchByBlogId(blogId);
        //删除博客与话题关系(批量)
        blogTopicRelationService.deleteRelationBatchByBlogId(blogId);
        //删除点赞关系(批量)并返回删除已点赞关系数目
        int blogLikeCount = blogLikeUserService.deleteLikeBatchByBlogId(blogId);
        //用户博客数-1
        userClient.userBlogCountMinusOne(userId);
        stringRedisTemplate.delete(PrefixKeyConstant.USER_INFO_PREFIX + userId);//删除用户信息缓存，使得用户博客数可以立即更新之前数据
        //用户总获得点赞数 - 该博客的所有点赞数
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String userLikeCountModifyKey = PrefixKeyConstant.USER_LIKE_COUNT_MODIFY_PREFIX + userId;
        ops.decrement(userLikeCountModifyKey,blogLikeCount);
        //删除redis中的博客评论数变化数与博客点赞数变化数（在高并发场景下如果博客已删除但是定时任务执行落盘，也不会报错，因为落盘定时任务中根据博客id查询博客为null时会跳过）
        String blogCommentCountModifyKey = PrefixKeyConstant.BLOG_COMMENT_COUNT_MODIFY_PREFIX + blogId;
        String blogLikeCountModifyKey = PrefixKeyConstant.BLOG_LIKE_COUNT_MODIFY_PREFIX + blogId;
        stringRedisTemplate.delete(blogCommentCountModifyKey);
        stringRedisTemplate.delete(blogLikeCountModifyKey);
        //话题总讨论数是不会因为博客或评论删除而减少的，它体现了该话题现在或曾经的讨论数，不会因为删博删评而减少。
    }

}
