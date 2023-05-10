package com.sichao.common.constant;

/**
 * @Description: RabbitMQ常量类
 * @author: sjc
 * @createTime: 2023年05月10日 14:44
 */
public class RabbitMQConstant {

    public static final String BLOG_EXCHANGE = "sichao-blog-exchange";//博客交换机
    public static final String BLOG_BINDING_TOPIC_QUEUE = "sichao.blog.binding.topic.queue";//博客绑定与话题关系的队列
    public static final String BLOG_BINDING_TOPIC_ROUTINGKEY = "sichao.blog.binding.topic";//博客绑定与话题关系的路由


    public static final String BLOG_AT_USER_QUEUE = "sichao.blog.at.user.queue";//博客@用户的队列
    public static final String BLOG_AT_USER_ROUTINGKEY = "sichao.blog.at.user";//博客@用户的路由
}
