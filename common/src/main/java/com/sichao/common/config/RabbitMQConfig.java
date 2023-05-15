package com.sichao.common.config;

import com.rabbitmq.client.ReturnCallback;
import com.sichao.common.constant.RabbitMQConstant;
import com.sichao.common.entity.MqMessage;
import com.sichao.common.mapper.MqMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Description: RabbitMQ配置类
 * @author: sjc
 * @createTime: 2023年05月10日 14:37
 * <p>
 * 容器中的Queue, Exchange, Binding会在监听消息的时候自动创建(在RabbitMQ)不存在的情况下
 * RabbitMQ只要有这些组件，@Bean声明属性就算发生变化也不会覆盖
 */
@Configuration
@Slf4j
public class RabbitMQConfig {
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MqMessageMapper mqMessageMapper;

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }

    //配置使用JSON格式
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 一、 ConfirmCallback：服务器收到消息就会回调
     *      1） publisher-confirm-type: correlated       #confirm机制:开启发送端消息抵达Broker确认
     *      2）设置确认回调
     * 二、ReturnCallback：消息正确抵达队列就会进行回调
     *      1） spring.rabbitmq.publisher-returns: true    spring.rabbitmq.template.mandatory: true
     *          ReturnCallback：当消息进入Exchange交换器时就进入回调，但是未进入队列时回调。这里有二种状态要注意当配置
     *          Mandatory：为true时,消息通过交换器无法匹配到队列会返回给生产者 并触发MessageReturn，为false时,匹配不到会直接被丢弃
     *      2）设置确认回调ReturnCallback
     * 三、消费端确认(保证每个消息都被正确消费，此时才可以broker删除这个消息)
     *      1）默认是自动确认，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *          问题：我们收到很多消息，自动回复给服务器ack，只有一个消息处理成功，然后服务器宕机了，发生消息丢失
     *          解决方法：手动确认。只要我们没有明确告诉MQ，货物被签收。没有Ack，消息就一直是unacked状态。即使Consumer连接
     *          宕机，消息也不会丢失，会重置为Ready状态，下一次有新的Consumer连接进来就会发给他
     *         spring.rabbitmq.listener.simple.acknowledge-mode=manual #手动ack消息，不使用默认的消费端确认
     *      2） 如何手动签收：
     *          channel.basicAck(deliveryTag, false);//手动确认签收货物，非批量模式
     *          channel.basicNack(deliveryTag, false， true)；//拒收货物（交货标签，是否批量退货，是否重新入队）
     *          channel.basicReject(deliveryTag, true)；//拒收货物（交货标签，是否重新入队）
     */
    public void initRabbitTemplate() {
        /**
         * 1、只要消息抵达Broker就ack=true
         * correlationData:当前消息的唯一关联数据,也就是生产者发送消息时携带的数据标识(这个是消息的唯一id)
         * ack：消息是否成功收到
         * cause：失败的原因
         */
        //消息发送到达服务器Broker，就调用此方法
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//            System.out.println("confirm...correlationData[" + correlationData + "]==)ack: [" + ack + "]==>cause: [" + cause + "]");
        });

        //mandatory这个值设置为true，代表如果消息丢失或者出现意外，将消息返回，而不是丢弃。
        rabbitTemplate.setMandatory(true);//配置文件中的此配置没生效，要在这里重新设置
        /**
         * returnedMessage.getMessage()     消息
         * returnedMessage.getReplyCode()   回应码
         * returnedMessage.getReplyText()   回应信息
         * returnedMessage.getExchange()    交换机
         * returnedMessage.getRoutingKey()  路由键
         */
        //当消息进入Exchange交换器时就进入回调
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            //异常消息持久化到MQ消息表
            //获取携带的数据标识
            String mqMessageId = (String) returnedMessage.getMessage().getMessageProperties().getHeaders().get("spring_returned_message_correlation");
            MqMessage mqMessage = new MqMessage();
            mqMessage.setId(mqMessageId);
            mqMessage.setStatus((byte)2);//错误抵达状态码
            mqMessageMapper.updateById(mqMessage);

            // 打印日志
//            log.info("ReturnCallback：" + "消息：" + returnedMessage.getMessage());
//            log.info("ReturnCallback：" + "回应码：" + returnedMessage.getReplyCode());
//            log.info("ReturnCallback：" + "回应信息：" + returnedMessage.getReplyText());
//            log.info("ReturnCallback：" + "交换机：" + returnedMessage.getExchange());
//            log.info("ReturnCallback：" + "路由键：" + returnedMessage.getRoutingKey());

        });

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
    public Queue blogBindingTopicQueue() {
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
    public Exchange blogExchange() {
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
    public Binding blogBindingTopicQueueBinding() {
        //给这个队列绑定交换机和路由，当生产者给这个交换机和路由发送消息时，会把消息发送给该队列
        return new Binding(RabbitMQConstant.BLOG_BINDING_TOPIC_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.BLOG_EXCHANGE,
                RabbitMQConstant.BLOG_BINDING_TOPIC_ROUTINGKEY,
                null);
    }
    //博客@用户的队列
    @Bean
    public Queue blogAtUserQueue() {
        return new Queue(RabbitMQConstant.BLOG_AT_USER_QUEUE, true, false, false);
    }
    //博客@用户的队列绑定交换机
    @Bean
    public Binding blogAtUserQueueBinding() {
        //给这个队列绑定交换机和路由，当生产者给这个交换机和路由发送消息时，会把消息发送给该队列
        return new Binding(RabbitMQConstant.BLOG_AT_USER_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.BLOG_EXCHANGE,
                RabbitMQConstant.BLOG_AT_USER_ROUTINGKEY,
                null);
    }

    //评论@用户的队列
    @Bean
    public Queue blogCommentAtUserQueue(){
        return new Queue(RabbitMQConstant.BLOG_COMMENT_AT_USER_QUEUE,true,false,false);
    }
    //评论@用户的队列绑定交换机
    @Bean
    public Binding blogCommentAtUserQueueBinding(){
        //给这个队列绑定交换机和路由，当生产者给这个交换机和路由发送消息时，会把消息发送给该队列
        return new Binding(RabbitMQConstant.BLOG_COMMENT_AT_USER_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.BLOG_EXCHANGE,
                RabbitMQConstant.BLOG_COMMENT_AT_USER_ROUTINGKEY,
                null);
    }


    //博客删除的队列
    @Bean
    public Queue blogDeleteQueue(){
        return new Queue(RabbitMQConstant.BLOG_DELETE_QUEUE,true,false,false);
    }
    //博客删除的队列绑定交换机
    @Bean
    public Binding blogDeleteQueueBinding(){
        //给这个队列绑定交换机和路由，当生产者给这个交换机和路由发送消息时，会把消息发送给该队列
        return new Binding(RabbitMQConstant.BLOG_DELETE_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.BLOG_EXCHANGE,
                RabbitMQConstant.BLOG_DELETE_ROUTINGKEY,
                null);
    }

}
