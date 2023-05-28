package com.sichao.blogService;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.entity.Blog;
import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.entity.vo.BlogVo;
import com.sichao.blogService.entity.vo.TopicTitleVo;
import com.sichao.blogService.mapper.BlogMapper;
import com.sichao.blogService.mapper.BlogTopicMapper;
import com.sichao.blogService.service.impl.BlogServiceImpl;
import com.sichao.common.constant.PrefixKeyConstant;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class ServiceBlogApplicationTests {
    @Autowired
    private RestHighLevelClient esClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private BlogTopicMapper blogTopicMapper;

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

    //时间差测试
//    public static void main(String[] args) {
//        LocalDateTime dateTime = LocalDateTime.now().minusSeconds(1);
//        System.out.println(LocalDateTime.now().compareTo(dateTime));//大-小，输入正数
//        System.out.println(dateTime.compareTo(dateTime));//相等，输入0
//        System.out.println(dateTime.compareTo(LocalDateTime.now()));//小-大，输入负数
//
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
//
//    }

    //@测试
//    public static void main(String[] args) {
//        StringBuffer strb=new StringBuffer();//用来拼接博客内容
//        //下面的代码不能有^与$,加了会对整个字符串进行匹配，如果字符串不是以@号开头则不匹配
//        //不加则可以用来对字符串中每个子串都进行匹配判断
//        Pattern referer_pattern = Pattern.compile("@([^@^\\s]{1,})([\\s]{0,1})");//@.+?[\\s:]
//        String msg = "qweqweqweqw@admin :奥德赛 @ijasdi:@jsidj @@1231 @加急 123";
//        String usname = "";
//        int idx = 0;
//        Matcher matchr = referer_pattern.matcher(msg);
//        //之前字符串中匹配到的位置不会在被匹配到，会往后开始匹配，配合while循环做到匹配整个字符串中所有符合正则表达式的子串
//        while (matchr.find()){//为true说明匹配，为false说明不匹配
//            String origion_str = matchr.group();//获取匹配到的字符串
//            System.out.println("origion_str："+origion_str);
//            String str = origion_str.substring(1, origion_str.length()).trim();//裁剪
//            System.out.println("str:"+str);
//            //matchr.start()：获得被匹配到的子串在原串的起始位置
//            strb.append(msg.substring(idx, matchr.start()));
//            strb.append(Constant.BLOG_AT_USER_HYPERLINK_PREFIX + "123" + Constant.BLOG_AT_USER_HYPERLINK_INFIX)
//                    .append(origion_str).append(Constant.BLOG_AT_USER_HYPERLINK_SUFFIX);
//            idx=matchr.start()+origion_str.length();
//
//
////            System.out.println(msg.substring(idx, matchr.start()));
//        }
//        strb.append(msg.substring(idx));
//        System.out.println(strb);
//    }

    //#测试
//    public static void main(String[] args) {
//        StringBuffer strb=new StringBuffer();//用来拼接博客内容
//        //正则表达式：^ {1}#{1}[^# ]{1,25}#{1} {1}$
//        // 5~29个字符，第一个与最后一个字符必须是空格，第二个与倒数第二个字符必须是'#'，其余的字符不能为'#'
//        //下面的代码不能有^与$,加了会对整个字符串进行匹配，如果字符串不是以#号开头或结尾则不匹配
//        //不加则可以用来对字符串中每个子串都进行匹配判断
//        Pattern referer_pattern = Pattern.compile(" {1}#{1}[^#]{1,25}#{1} {1}");
//
//        System.out.println("=================");
//        String msg = "qweqweqweqw #admi# n 奥德赛 #@ij# asd#i @js #idj @@# 1#1111 加急";
//        //String msg = " #aaaaaaaaaaaaaaaaaaaaaaaaa# ";
//        String usname = "";
//        int idx = 0;
//        Matcher matchr = referer_pattern.matcher(msg);
//        //之前字符串中匹配到的位置不会在被匹配到，会往后开始匹配，配合while循环做到匹配整个字符串中所有符合正则表达式的子串
//        while (matchr.find()){//为true说明匹配，为false说明不匹配。
//            String origion_str = matchr.group();//获取匹配到的字符串
//            System.out.println("origion_str："+origion_str);
//            String str = origion_str.substring(2, origion_str.length()-2).trim();//裁剪
//            System.out.println("str:"+str);
//            //matchr.start()：获得被匹配到的子串在原串的起始位置
//            strb.append(msg.substring(idx, matchr.start()));
//            strb.append(Constant.BLOG_AT_TOPIC_HYPERLINK_PREFIX + "123" + Constant.BLOG_AT_TOPIC_HYPERLINK_INFIX)
//                    .append(origion_str).append(Constant.BLOG_AT_TOPIC_HYPERLINK_SUFFIX);
//            idx=matchr.start()+origion_str.length();
//
////            System.out.println(msg.substring(idx, matchr.start()));
//        }
//        strb.append(msg.substring(idx));
//        System.out.println(strb);
//    }

}
