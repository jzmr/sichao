package com.sichao.blogService.crontask;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.entity.vo.TopicTitleVo;
import com.sichao.blogService.mapper.BlogTopicMapper;
import com.sichao.blogService.service.BlogTopicService;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.entity.TaskExecutionInfo;
import com.sichao.common.mapper.TaskExecutionInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.geom.QuadCurve2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @Description: 博客模块定时任务类
 * @author: sjc
 * @createTime: 2023年05月08日 14:27
 */
@Slf4j
@Component//交给spring管理
//@EnableScheduling//开启定时任务（已在Scheduled配置文件配置了开启定时任务与异步任务）
public class blogServiceCronTask {
    public static final int hotTopicSize=100; //热搜榜长度，前50个话题是要展示的，后50个话题备用

    @Autowired
    private BlogTopicService blogTopicService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TaskExecutionInfoMapper taskExecutionInfoMapper;


    //每隔1个小时，动态计算三日内创建的话题热度
    @Async
    @Scheduled(cron = "0 0 * * * ?")//每小时第0分0秒时启动，前面的0不写会导致，0分的每一秒都会执行定时任务
//    @Scheduled(cron = "1 * * * * ?")//每分钟的第一秒
    public void refreshHotTopic() {
        log.info("refreshHotTopic定时任务开始");
        String cronTaskLock = PrefixKeyConstant.BLOG_CRON_TASK_LOCK_PREFIX + "refreshHotTopic";

        //定时任务要使用分布式锁，使得该定时任务只被执行一次
        RLock lock = redissonClient.getLock(cronTaskLock);
        //尝试获得锁
        if(lock.tryLock()){
            //记录任务执行信息，记录任务开始时间
            TaskExecutionInfo taskInfo = new TaskExecutionInfo(
                    "refreshHotTopic",
                    "动态计算三日内创建的话题热度",
                    LocalDateTime.now(),(byte) 0);
            taskExecutionInfoMapper.insert(taskInfo);

            try {
                // 业务代码
                //获得最近三天以来新建的话题
                QueryWrapper<BlogTopic> wrapper = new QueryWrapper<>();
                wrapper.eq("status",1);//话题可用
                //TODO 开发阶段就不设置3天的限制了
//                wrapper.ge("create_time", LocalDateTime.now().minusDays(3));
                List<BlogTopic> list = blogTopicService.list(wrapper);
                //准备redis与key
                String hotTopicKey = PrefixKeyConstant.BLOG_HOT_TOPIC_KEY;//热搜榜key
                ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();

                //删除热搜榜,之后在创建，作用是清空热搜榜，避免旧话题长期积压在redis中
                stringRedisTemplate.delete(hotTopicKey);

                //计算热度公式：热度 = 总讨论数 / (当前时间 - 创建时间 + 2) ^ 1.5  （时间衰减方法）
                for (BlogTopic topic : list) {
                    try {
                        //总讨论数 TODO 要加上缓存中的讨论数修改数
                        int totalDiscussion = topic.getTotalDiscussion();

                        //相差小时数=当前时间 - 创建时间
                        LocalDateTime createTime = topic.getCreateTime();
                        LocalDateTime dateTimeNow=LocalDateTime.now();
                        Duration duration = Duration.between(createTime, dateTimeNow);
                        long diffHours = duration.toHours();//相差小时
                        //计算热度
                        double hotness=(double)totalDiscussion/Math.pow(diffHours+2,1.5);

                        //准备要保存到redis中的数据（话题id与话题title）
                        TopicTitleVo topicTitleVo = new TopicTitleVo(topic.getId(),topic.getTopicTitle());
                        String str = JSON.toJSONString(topicTitleVo);
                        //保存带redis的SortedSet类型的key中（热搜榜）
                        //如果key不存在则创建并加入value；如果value不存在于key中，则添加；如果value存在于key中，则修改分数为hotness)
                        zSet.add(hotTopicKey,str,hotness);
                    }catch (Exception e) {
                        //异常直接捕获，避免因为一个话题的异常导致热搜功能无法使用（缺少一两个有异常的话题是可以接受的）
                        if(taskInfo.getExceptionInfo()==null){
                            taskInfo.setExceptionInfo(topic.getId()+",");//保存异常的话题id
                        }else {
                            taskInfo.setExceptionInfo(taskInfo.getExceptionInfo()+topic.getId()+",");//保存异常的话题id
                        }
                        log.warn("话题热度计算异常=>{\"id\":\""+topic.getId()+"\",\"topic_title\":\""+topic.getTopicTitle()+"\"}");
                    }
                }

                //长度超过hotTopicSize时，删除排名hotTopicSize之后的话题
                //保存在redis中的zSet类型中的数据，默认是升序，查询时在命令的Z后面添加REV可降序查询
                Long size = zSet.size(hotTopicKey);
                if(size!=null && size>hotTopicSize){
                    //删除
                    zSet.removeRange(hotTopicKey,0,size-hotTopicSize-1);
                }

                //任务执行成功
                taskInfo.setStatus((byte)1);
                taskInfo.setEndTime(LocalDateTime.now());//设置任务结束时间
                taskExecutionInfoMapper.updateById(taskInfo);
                log.info("refreshHotTopic定时任务结束");
            }catch (Exception e) {
                //任务执行失败
                taskInfo.setStatus((byte)2);
                taskExecutionInfoMapper.updateById(taskInfo);
                log.error("refreshHotTopic定时任务执行失败");
            }finally {
                lock.unlock();//解锁
            }
        }else {
            // 未获得锁，忽略
        }
    }

    //话题总讨论数自增数定时任务落盘 TODO
}
