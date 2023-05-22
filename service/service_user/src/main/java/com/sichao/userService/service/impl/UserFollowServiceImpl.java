package com.sichao.userService.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.exceptionhandler.sichaoException;
import com.sichao.common.utils.RandomSxpire;
import com.sichao.userService.entity.User;
import com.sichao.userService.entity.UserFollow;
import com.sichao.userService.entity.vo.FollowListVo;
import com.sichao.userService.mapper.UserFollowMapper;
import com.sichao.userService.service.UserFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.userService.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * 用户关注用户关系id 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-05-03
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {
    @Autowired
    public UserService userService;//用户服务类
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    // 关注用户
    @Transactional
    @Override
    public void follow(String userId, String followingId) {
        //查询
        User byId = userService.getById(followingId);
        if(byId==null)throw new sichaoException(Constant.FAILURE_CODE,"关注用户不存在");

        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id",userId);
        wrapper.eq("following_id",followingId);
        UserFollow follow = baseMapper.selectOne(wrapper);
        int isSuccess=0;
        if(follow!=null){//存在记录，修改状态
            if(!follow.getStatus()){//未关注,则修改状态未关注
                follow.setStatus(true);
                follow.setUpdateTime(null);//避免修改时间自动填充失效
                isSuccess = baseMapper.updateById(follow);//返回操作数据条目数：1
            }
        }else {//不存在记录，插入数据
            follow = new UserFollow();
            follow.setFollowerId(userId);
            follow.setFollowingId(followingId);
            isSuccess = baseMapper.insert(follow);//返回操作数据条目数：1
        }

        //关注成功
        if(isSuccess == 1){
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String followerModifyKey = PrefixKeyConstant.USER_FOLLOWER_MODIFY_PREFIX + followingId;//用户粉丝变化数key
            String followingModifyKey = PrefixKeyConstant.USER_FOLLOWING_MODIFY_PREFIX + userId;//用户关注变化数key

            //关注人的关注用户数+1
            ops.increment(followingModifyKey);//自增，如果key不存在，则先创建整个key且值为0，而后再自增
            //被关注人的粉丝数+1
            ops.increment(followerModifyKey);


            ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
            LocalDateTime updateTime = follow.getUpdateTime();//转换成Unix时间戳//以修改数据转换成时间戳
            long timestamp = updateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            //将被关注人id加入关注列表缓存
            String followingZSetKey = PrefixKeyConstant.USER_FOLLOWING_LIST_PREFIX + userId;//用户关注列表的key
            if(Boolean.FALSE.equals(stringRedisTemplate.hasKey(followingZSetKey))){//key不存在
                getFollowingSetCache(userId);//查询关注列表
            }
            zSet.add(followingZSetKey,followingId,timestamp);
            //将当前用户id加入被关注人的粉丝列表缓存
            String followerZSetKey = PrefixKeyConstant.USER_FOLLOWER_LIST_PREFIX + followingId;//用户粉丝列表的key
            if(Boolean.FALSE.equals(stringRedisTemplate.hasKey(followerZSetKey))){//key不存在
                getFollowerSetCache(followingId);//查询粉丝列表
            }
            zSet.add(followerZSetKey,userId,timestamp);
            //删除当前用户关注的用户的列表缓存
            String followingBlogZSetKey = PrefixKeyConstant.BLOG_FOLLOWING_BLOG_PREFIX + userId;//当前用户关注的用户的博客id的key
            stringRedisTemplate.delete(followingBlogZSetKey);
        }
    }
    @Transactional
    //取关用户
    @Override
    public void unfollow(String userId, String followingId) {
        //查询
        User byId = userService.getById(followingId);
        if(byId==null)throw new sichaoException(Constant.FAILURE_CODE,"关注用户不存在");

        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id",userId);
        wrapper.eq("following_id",followingId);
        UserFollow one = baseMapper.selectOne(wrapper);
        int isSuccess=0;
        if(one!=null){//存在记录，修改状态
            if(one.getStatus()){//已关注,则修改状态未关注
                one.setStatus(false);
                one.setUpdateTime(null);//避免修改时间自动填充失效
                isSuccess = baseMapper.updateById(one);//返回操作数据条目数：1
            }
        }
        //取关成功
        if(isSuccess==1){
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String followerModifyKey = PrefixKeyConstant.USER_FOLLOWER_MODIFY_PREFIX + followingId;//用户粉丝变化数key
            String followingModifyKey = PrefixKeyConstant.USER_FOLLOWING_MODIFY_PREFIX + userId;//用户关注变化数key

            //关注人的关注用户数-1
            ops.decrement(followingModifyKey);//自减，如果key不存在，则先创建整个key且值为0，而后再自减
            //被关注人的粉丝数-1
            ops.decrement(followerModifyKey);
            //将被关注人id从关注列表缓存中删除
            ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
            String followingZSetKey = PrefixKeyConstant.USER_FOLLOWING_LIST_PREFIX + userId;//用户关注列表的key
            if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(followingZSetKey))){//key存在
                zSet.remove(followingZSetKey,followingId);
            }
            //将当前用户id从被关注人的粉丝列表缓存中删除
            String followerZSetKey = PrefixKeyConstant.USER_FOLLOWER_LIST_PREFIX + followingId;//用户粉丝列表的key
            if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(followerZSetKey))){//key存在
                zSet.remove(followerZSetKey,userId);
            }
            //删除当前用户关注的用户的列表缓存
            String followingBlogZSetKey = PrefixKeyConstant.BLOG_FOLLOWING_BLOG_PREFIX + userId;//当前用户关注的用户的博客id的key
            stringRedisTemplate.delete(followingBlogZSetKey);
        }
    }

    // 查看当前用户是否关注某位其他用户
    @Override
    public boolean getFollowStatus(String userId, String id) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id",userId);//粉丝id
        wrapper.eq("following_id",id);//被关注者id
        wrapper.select("status");
        UserFollow userFollow = baseMapper.selectOne(wrapper);
        if(userFollow == null)return false;
        return userFollow.getStatus();
    }
    //查看用户关注列表
    @Override
    public List<FollowListVo> getFollowingList(String currentId, String id) {//currentId：当前用户id     id：要查询关注列表的id
        List<FollowListVo> list = baseMapper.getFollowingList(currentId,id);
        return list;
    }
    //查看用户粉丝列表
    @Override
    public List<FollowListVo> getFollowerList(String currentId, String id) {
        List<FollowListVo> list = baseMapper.getFollowerList(currentId,id);
        return list;
    }

    //查询当前用户关注的用户的昵称
    @Override
    public List<String> getFollowingNicknameList(String userId) {
        List<String> nicknameList = baseMapper.getFollowingNicknameList(userId);
        return nicknameList;
    }


    //获取用户关注列表（使用redis的zSet类型缓存）
    @Override
    public Set<String> getFollowingSetCache(String userId){
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        String followingZSetLockKey = PrefixKeyConstant.USER_FOLLOWING_LIST_LOCK_PREFIX + userId;//用户关注列表查询锁key
        String followingZSetKey = PrefixKeyConstant.USER_FOLLOWING_LIST_PREFIX + userId;//用户关注列表的key

        //查看缓存中是否有数据(-1表示到最后一个元素)
        Set<String> set = zSet.range(followingZSetKey, 0, -1);
        if(set==null || set.isEmpty()) {
            RLock lock = redissonClient.getLock(followingZSetLockKey);
            lock.lock();//加锁，阻塞
            try {//双查机制，在锁内再查一遍缓存中是否有数据
                set = zSet.range(followingZSetKey, 0, -1);
                if(set==null || set.isEmpty()) {//缓存不存在
                    //查询当前用户的所有关注用户
                    QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
                    wrapper.eq("follower_id", userId);
                    wrapper.eq("status", 1);
                    wrapper.select("following_id", "update_time");
                    List<UserFollow> followList = baseMapper.selectList(wrapper);
                    if (followList != null && !followList.isEmpty()) {
                        for (UserFollow userFollow : followList) {
                            //转换成Unix时间戳//以修改数据转换成时间戳
                            LocalDateTime updateTime = userFollow.getUpdateTime();
                            long timestamp = updateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

                            //将关注的用户的id保存到redis中的zSet类型中，使用时间戳自动排序
                            zSet.add(followingZSetKey, userFollow.getFollowingId(), timestamp);
                        }
                        //为zSet的key设置生存时长
                        stringRedisTemplate.expire(followingZSetKey,
                                Constant.ONE_HOURS_EXPIRE + RandomSxpire.getRandomSxpire(),//1小时
                                TimeUnit.MILLISECONDS);
                    } else {
                        //如果查询的数据为空，则向缓存中写入空串，并设置5分钟（短期）的过期时间（避免缓存穿透）
                        zSet.add(followingZSetKey, "", 0);
                        //为key设置生存时长
                        stringRedisTemplate.expire(followingZSetKey,
                                Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),//5分钟
                                TimeUnit.MILLISECONDS);
                    }
                    set = zSet.range(followingZSetKey, 0, -1);
                }
            }finally {
                lock.unlock();//解锁
            }
        }
        return set;
    }

    //获取用户粉丝列表（使用redis的zSet类型缓存）
    @Override
    public Set<String> getFollowerSetCache(String userId){
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        String followerZSetLockKey = PrefixKeyConstant.USER_FOLLOWER_LIST_LOCK_PREFIX + userId;//用户粉丝列表查询锁key
        String followerZSetKey = PrefixKeyConstant.USER_FOLLOWER_LIST_PREFIX + userId;//用户粉丝列表的key

        //查看缓存中是否有数据(-1表示到最后一个元素)
        Set<String> set = zSet.range(followerZSetKey, 0, -1);
        if(set==null || set.isEmpty()) {
            RLock lock = redissonClient.getLock(followerZSetLockKey);
            lock.lock();//加锁，阻塞
            try {//双查机制，在锁内再查一遍缓存中是否有数据
                set = zSet.range(followerZSetKey, 0, -1);
                if(set==null || set.isEmpty()) {//缓存不存在
                    //查询当前用户的所有粉丝用户
                    QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
                    wrapper.eq("following_id", userId);
                    wrapper.eq("status", 1);
                    wrapper.select("follower_id", "update_time");
                    List<UserFollow> followList = baseMapper.selectList(wrapper);
                    if (followList != null && !followList.isEmpty()) {
                        for (UserFollow userFollow : followList) {
                            //转换成Unix时间戳//以修改数据转换成时间戳
                            LocalDateTime updateTime = userFollow.getUpdateTime();
                            long timestamp = updateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

                            //将关注的用户的id保存到redis中的zSet类型中，使用时间戳自动排序
                            zSet.add(followerZSetKey, userFollow.getFollowerId(), timestamp);
                        }
                        //为zSet的key设置生存时长
                        stringRedisTemplate.expire(followerZSetKey,
                                Constant.ONE_HOURS_EXPIRE + RandomSxpire.getRandomSxpire(),//1小时
                                TimeUnit.MILLISECONDS);
                    } else {
                        //如果查询的数据为空，则向缓存中写入空串，并设置5分钟（短期）的过期时间（避免缓存穿透）
                        zSet.add(followerZSetKey, "", 0);
                        //为key设置生存时长
                        stringRedisTemplate.expire(followerZSetKey,
                                Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),//5分钟
                                TimeUnit.MILLISECONDS);
                    }
                    set = zSet.range(followerZSetKey, 0, -1);
                }
            }finally {
                lock.unlock();//解锁
            }
        }
        return set;
    }
}
