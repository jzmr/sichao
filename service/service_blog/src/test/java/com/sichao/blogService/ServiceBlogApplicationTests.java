package com.sichao.blogService;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.entity.vo.TopicTitleVo;
import com.sichao.blogService.mapper.BlogTopicMapper;
import com.sichao.common.constant.PrefixKeyConstant;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class ServiceBlogApplicationTests {
    @Autowired
    private BlogTopicMapper blogTopicMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void hotTopicTest() {
        //获得最近三天以来新建的话题
        QueryWrapper<BlogTopic> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", LocalDateTime.now().minusDays(3));
        List<BlogTopic> list = blogTopicMapper.selectList(wrapper);

        String hotTopicKey = PrefixKeyConstant.BLOG_HOT_TOPIC_KEY;
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();

        //计算热度公式：热度 = 总讨论数 / (当前时间 - 创建时间 + 2) ^ 1.5  （时间衰减方法）
        for (BlogTopic topic : list) {
            //总讨论数
            int totalDiscussion = topic.getTotalDiscussion();
            //相差小时数=当前时间 - 创建时间
            LocalDateTime createTime = topic.getCreateTime();
            LocalDateTime dateTimeNow=LocalDateTime.now();
            Duration duration = Duration.between(createTime, dateTimeNow);
            long diffHours = duration.toHours();//相差小时
            //计算热度
            double hotness=(double)totalDiscussion/(diffHours+2)*1.5;

            //准备要保存到redis中的数据（话题id与话题title）
            TopicTitleVo topicTitleVo = new TopicTitleVo(topic.getId(),topic.getTopicTitle());
            String str = JSON.toJSONString(topicTitleVo);
            //保存带redis的SortedSet类型的key中（热搜榜）
            zSet.add(hotTopicKey,str,hotness);//如果value不存在于key中，则添加；如果value存在于key中，则修改分数为hotness，)
        }
    }

    public static void main(String[] args) {
//        LocalDateTime dateTime = LocalDateTime.now().minusSeconds(1);
//        System.out.println(LocalDateTime.now().compareTo(dateTime));//大-小，输入正数
//        System.out.println(dateTime.compareTo(dateTime));//相等，输入0
//        System.out.println(dateTime.compareTo(LocalDateTime.now()));//小-大，输入负数

//        LocalDateTime dateTime1=LocalDateTime.of(2022, 10, 8, 10, 30, 10);
//        LocalDateTime dateTime2=LocalDateTime.now();
//
//        Duration duration = Duration.between(dateTime1, dateTime2);
//        System.out.println(dateTime1 + " 与 " + dateTime2 + " 间隔  " + "\n"   //2022-10-08T10:30:10 与 2023-05-08T15:17:05.047010800 间隔
//                + " 天 :" + duration.toDays() + "\n"     //天 :212
//                + " 时 :" + duration.toHours() + "\n"        //时 :5092
//                + " 分 :" + duration.toMinutes() + "\n"      //分 :305566
//                + " 秒 :" + duration.getSeconds() + "\n"     //秒 :18334015
//                + " 毫秒 :" + duration.toMillis() + "\n"      //毫秒 :18334015047
//                + " 纳秒 :" + duration.toNanos() + "\n"       //纳秒 :18334015047010800
//        );

    }

}
