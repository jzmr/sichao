package com.sichao.common.config;

import com.sichao.common.constant.RabbitMQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Description: RabbitMQ配置类
 * @author: sjc
 * @createTime: 2023年05月10日 14:37
 *
 * 容器中的Queue, Exchange, Binding会在监听消息的时候自动创建(在RabbitMQ)不存在的情况下
 * RabbitMQ只要有这些组件，@Bean声明属性就算发生变化也不会覆盖
 */
@Configuration
public class RabbitMQConfig {

    //配置使用JSON格式
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }



    /**
     * Queue(String name, 队列名字
     * boolean durable, 是否持久化
     * boolean exclusive, 是否排他
     * boolean autoDelete，是否自动删除
     * Map<String, Object) arguments)属性
     */
    //博客绑定与话题关系队列
    @Bean//加入Bean容器，会第一次监听时在队列不存在的情况下自动创建队列
    public Queue blogBindingTopicQueue(){
        return new Queue(RabbitMQConstant.BLOG_BINDING_TOPIC_QUEUE, true, false, false);
    }
    /**
     * String name, 交换机名称
     * boolean durable, 是否持久化
     * boolean autoDelete,  是否自动删除
     * Map<String, Object> arguments 属性
     */
    //博客交换机
    @Bean
    public Exchange blogExchange(){
        return new TopicExchange(RabbitMQConstant.BLOG_EXCHANGE, true, false);
    }
    /**
     * String destination,  指定队列名
     * DestinationType destinationType, 指定绑定类型
     * String exchange,     指定交换机名
     * String routingKey,   指定路由
     * @Nullable Map<String, Object> arguments 附带属性
     */
    //博客绑定与话题关系队列绑定交换机
    @Bean
    public Binding blogBindingTopicQueueBinding(){
        //给这个队列绑定交换机和路由，当生产者给这个交换机和路由发送消息时，会把消息发送给该队列
        return new Binding(RabbitMQConstant.BLOG_BINDING_TOPIC_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.BLOG_EXCHANGE,
                RabbitMQConstant.BLOG_BINDING_TOPIC_ROUTINGKEY,
                null);
    }


    //博客@用户的队列
    @Bean
    public Queue blogAtUserQueue(){
        return new Queue(RabbitMQConstant.BLOG_AT_USER_QUEUE, true, false, false);
    }
    //博客@用户的队列绑定交换机
    @Bean
    public Binding blogAtUserQueueBinding(){
        //给这个队列绑定交换机和路由，当生产者给这个交换机和路由发送消息时，会把消息发送给该队列
        return new Binding(RabbitMQConstant.BLOG_AT_USER_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.BLOG_EXCHANGE,
                RabbitMQConstant.BLOG_AT_USER_ROUTINGKEY,
                null);
    }

}
