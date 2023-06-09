package com.sichao.common.constant;

/**
 * @Description: RabbitMQ常量类
 * @author: sjc
 * @createTime: 2023年05月10日 14:44
 */
public class RabbitMQConstant {

    public static final String BLOG_EXCHANGE = "sichao-blog-exchange";//博客模块交换机
    public static final String BLOG_BINDING_TOPIC_QUEUE = "sichao.blog.binding.topic.queue";//博客绑定与话题关系的队列
    public static final String BLOG_BINDING_TOPIC_ROUTINGKEY = "sichao.blog.binding.topic";//博客绑定与话题关系的路由

    public static final String BLOG_AT_USER_QUEUE = "sichao.blog.at.user.queue";//博客@用户的队列
    public static final String BLOG_AT_USER_ROUTINGKEY = "sichao.blog.at.user";//博客@用户的路由

    public static final String BLOG_COMMENT_AT_USER_QUEUE = "sichao.blog.comment.at.user.queue";//评论@用户的队列
    public static final String BLOG_COMMENT_AT_USER_ROUTINGKEY = "sichao.blog.comment.at.user";//评论@用户的路由

    public static final String BLOG_DELETE_QUEUE = "sichao.blog.delete.queue";//博客删除的队列
    public static final String BLOG_DELETE_ROUTINGKEY = "sichao.blog.delete";//博客删除的路由
    public static final String BLOG_PUBLISH_AFTER_QUEUE = "sichao.blog.publish.after.queue";//博客发布后续处理的队列
    public static final String BLOG_PUBLISH_AFTER_ROUTINGKEY = "sichao.blog.publish.after";//博客发布后续处理的路由

    public static final String MESSAGE_EXCHANGE = "sichao-message-exchange";//消息模块交换机
    //（将userId与message发送到队列中广播给使用订阅的消费者，该消费者中存放session的map集合有与userId一样的key时，向该session发送message）
    public static final String MESSAGE_SEND_QUEUE = "sichao.message.send.queue";//消息发送的队列
    public static final String MESSAGE_SEND_ROUTINGKEY = "sichao.message.send.";//消息发送的路由
}
