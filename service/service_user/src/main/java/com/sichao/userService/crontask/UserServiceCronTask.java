package com.sichao.userService.crontask;

import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.entity.TaskExecutionInfo;
import com.sichao.common.exceptionhandler.sichaoException;
import com.sichao.common.mapper.TaskExecutionInfoMapper;
import com.sichao.userService.entity.User;
import com.sichao.userService.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

/**
 * @Description: 用户模块定时任务类
 * @author: sjc
 * @createTime: 2023年05月04日 21:25
 */
@Slf4j
@Component//交给spring管理
//@EnableScheduling//开启定时任务（已在Scheduled配置文件配置了开启定时任务与异步任务）
public class UserServiceCronTask {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TaskExecutionInfoMapper taskExecutionInfoMapper;

    //将缓存中的用户表中的关注变化数、粉丝变化数、总获得点赞数落盘到数据库中（To Disk 落盘）
    @Async
    @Scheduled(cron = "* * 3 * * ?")//每天三点
//    @Scheduled(cron = "1 * * * * ?")//每分钟的第一秒
    public void userInfoCountModifyToDisk() throws InterruptedException {
        log.info("userInfoCountModifyToDisk定时任务开始");
        String cronTaskLock = PrefixKeyConstant.USER_CRON_TASK_LOCK_PREFIX + "userInfoCountModifyToDisk";//定时任务锁
        String userInfoPrefix = PrefixKeyConstant.USER_INFO_PREFIX;// 用户信息key前缀
        String followerModifyPrefix = PrefixKeyConstant.USER_FOLLOWER_MODIFY_PREFIX;//用户粉丝变化数key前缀
        String followingModifyPrefix = PrefixKeyConstant.USER_FOLLOWING_MODIFY_PREFIX;//用户关注变化数key前缀
        String userLikeCountModifyPrefix = PrefixKeyConstant.USER_LIKE_COUNT_MODIFY_PREFIX;//用户总获得点赞变化数key前缀

        //定时任务要使用分布式锁，使得该定时任务只被执行一次，且如果定时任务出现异常，要捕获后写入定时任务异常表
        // TODO 是否可以不用分布式锁，让每一个节点都去执行。因为使用lua脚本保证了原子性，如果脚本执行返回为null则不进行落盘操作，如果该key不存在也不进行落盘操作
        RLock lock = redissonClient.getLock(cronTaskLock);
        //尝试获得锁
        if (lock.tryLock()) {//获得锁
            //记录任务执行信息，记录任务开始时间
            TaskExecutionInfo taskInfo = new TaskExecutionInfo(
                    "userInfoCountModifyToDisk",
                    "将缓存中的用户表中的关注变化数、粉丝变化数、总获得点赞数落盘到数据库中",
                    LocalDateTime.now(), (byte) 0);
            taskExecutionInfoMapper.insert(taskInfo);

            try {
                // 业务代码
                //处理用户粉丝变化数
                Set<String> followerModifyKeys = stringRedisTemplate.keys(followerModifyPrefix + "*");
                if (followerModifyKeys != null) {
                    for (String key : followerModifyKeys) {
                        //处理多个数据时，当处理到某个数据出现异常是，记录异常信息并抛出异常给外围try...catch捕获
                        try {
                            //lua脚本：获取指定key的value值并删除key。使用lua脚本保证操作多次操作的原子性
                            String script = """
                                    local num= redis.call('GET',KEYS[1])
                                    redis.call('DEL',KEYS[1])
                                    return num
                                    """;
                            long modifyCount = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(key));
                            //落盘到用户表中
                            String userId = key.substring(key.lastIndexOf(':') + 1);//裁取用户id
                            User user = userService.getById(userId);
                            if(user == null)continue;//无数据说明跳过
                            user.setFollowerCount((int) (user.getFollowerCount() + modifyCount));
                            userService.updateById(user);
                            //删除用户信息缓存
                            stringRedisTemplate.delete(userInfoPrefix + userId);
                        } catch (Exception e) {
                            taskInfo.setExceptionInfo("处理用户\"" + key.substring(key.lastIndexOf(':') + 1) + "\"粉丝变化数时出现异常");
                            throw new sichaoException(Constant.FAILURE_CODE,
                                    "处理用户\"" + key.substring(key.lastIndexOf(':') + 1) + "\"粉丝变化数时出现异常");
                        }

                    }
                }
                //处理用户关注变化数
                Set<String> followingModifyKeys = stringRedisTemplate.keys(followingModifyPrefix + "*");
                if (followingModifyKeys != null) {
                    for (String key : followingModifyKeys) {
                        //处理多个数据时，当处理到某个数据出现异常是，记录异常信息并抛出异常给外围try...catch捕获
                        try {
                            //lua脚本：获取指定key的value值并删除key。使用lua脚本保证操作多次操作的原子性
                            String script = """
                                    local num= redis.call('GET',KEYS[1])
                                    redis.call('DEL',KEYS[1])
                                    return num
                                    """;
                            long modifyCount = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(key));
                            //落盘到用户表中
                            String userId = key.substring(key.lastIndexOf(':') + 1);//裁取用户id
                            User user = userService.getById(userId);
                            if(user == null)continue;//无数据说明跳过
                            user.setFollowingCount((short) (user.getFollowingCount() + modifyCount));
                            userService.updateById(user);
                            //删除用户信息缓存
                            stringRedisTemplate.delete(userInfoPrefix + userId);
                        } catch (Exception e) {
                            taskInfo.setExceptionInfo("处理用户\"" + key.substring(key.lastIndexOf(':') + 1) + "\"关注变化数时出现异常");
                            throw new sichaoException(Constant.FAILURE_CODE,
                                    "处理用户\"" + key.substring(key.lastIndexOf(':') + 1) + "\"关注变化数时出现异常");
                        }
                    }
                }
                //处理用户总获得点赞变化数
                Set<String> likeCountModifyKeys = stringRedisTemplate.keys(userLikeCountModifyPrefix + "*");
                if (likeCountModifyKeys != null) {
                    for (String key : likeCountModifyKeys) {
                        //处理多个数据时，当处理到某个数据出现异常是，记录异常信息并抛出异常给外围try...catch捕获
                        try {
                            //lua脚本：获取指定key的value值并删除key。使用lua脚本保证操作多次操作的原子性
                            String script = """
                                    local num= redis.call('GET',KEYS[1])
                                    redis.call('DEL',KEYS[1])
                                    return num
                                    """;
                            long modifyCount = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(key));
                            //落盘到用户表中
                            String userId = key.substring(key.lastIndexOf(':') + 1);//裁取用户id
                            User user = userService.getById(userId);
                            if(user == null)continue;//无数据说明跳过
                            user.setTotalLikeCount((int) (user.getTotalLikeCount()+modifyCount));
                            userService.updateById(user);
                            //删除用户信息缓存
                            stringRedisTemplate.delete(userInfoPrefix + userId);
                        } catch (Exception e) {
                            taskInfo.setExceptionInfo("处理用户\"" + key.substring(key.lastIndexOf(':') + 1) + "\"总获得点赞变化数时出现异常");
                            throw new sichaoException(Constant.FAILURE_CODE,
                                    "处理用户\"" + key.substring(key.lastIndexOf(':') + 1) + "\"总获得点赞变化数时出现异常");
                        }
                    }
                }


                //任务执行成功
                taskInfo.setStatus((byte) 1);
                taskInfo.setEndTime(LocalDateTime.now());//设置任务结束时间
                taskExecutionInfoMapper.updateById(taskInfo);
                log.info("userInfoCountModifyToDisk定时任务结束");
            } catch (Exception e) {
                //任务执行失败
                taskInfo.setStatus((byte) 2);
                taskExecutionInfoMapper.updateById(taskInfo);
                log.error("userInfoCountModifyToDisk定时任务执行失败");
            } finally {
                lock.unlock();//解锁
            }
        } else {
            // 未获得锁，忽略
        }
    }

}
