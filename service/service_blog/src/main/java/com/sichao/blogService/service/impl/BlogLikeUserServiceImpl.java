package com.sichao.blogService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.entity.Blog;
import com.sichao.blogService.entity.BlogLikeUser;
import com.sichao.blogService.mapper.BlogLikeUserMapper;
import com.sichao.blogService.mapper.BlogMapper;
import com.sichao.blogService.service.BlogLikeUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.exceptionhandler.sichaoException;
import com.sichao.common.utils.RandomSxpire;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户点赞博客关系表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogLikeUserServiceImpl extends ServiceImpl<BlogLikeUserMapper, BlogLikeUser> implements BlogLikeUserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private RedissonClient redissonClient;

    //点赞博客
    @Override
    public void likeBlog(String userId, String blogId) {
        //查看博客是否存在
        Blog blog = blogMapper.selectById(blogId);
        if(blog==null){
            throw new sichaoException(Constant.FAILURE_CODE,"点赞异常，博客不存在");
        }

        QueryWrapper<BlogLikeUser> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("blog_id",blogId);
        BlogLikeUser one = baseMapper.selectOne(wrapper);
        int isSuccess=0;
        if(one != null){//存在记录，修改状态
            if(!one.getStatus()){
                one.setStatus(true);
                one.setUpdateTime(null);//避免修改时间自动填充失效
                isSuccess=baseMapper.updateById(one);//返回操作数据条目数：1
            }
        }else {//不存在记录，插入数据
            BlogLikeUser blogLikeUser = new BlogLikeUser();
            blogLikeUser.setUserId(userId);
            blogLikeUser.setBlogId(blogId);
            isSuccess=baseMapper.insert(blogLikeUser);
        }
        //点赞成功
        if(isSuccess==1){
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String blogLikeCountModifyKey = PrefixKeyConstant.BLOG_LIKE_COUNT_MODIFY_PREFIX + blogId;//博客点赞数变化数key

            String userLikeCountModifyKey = PrefixKeyConstant.USER_LIKE_COUNT_MODIFY_PREFIX + blog.getCreatorId();//用户总获得点赞数变化数key

            //博客点赞数变化数+1
            ops.increment(blogLikeCountModifyKey);//自增，如果key不存在，则先创建整个key且值为0，而后再自增
            //被点赞博客的作者的总点赞数变化数+1
            ops.increment(userLikeCountModifyKey);
        }
    }

    //取消点赞博客
    @Override
    public void unlikeBlog(String userId, String blogId) {
        QueryWrapper<BlogLikeUser> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("blog_id",blogId);
        BlogLikeUser one = baseMapper.selectOne(wrapper);
        int isSuccess=0;
        if(one != null){//存在记录，修改状态
            if(one.getStatus()){
                one.setStatus(false);
                one.setUpdateTime(null);//避免修改时间自动填充失效
                isSuccess=baseMapper.updateById(one);//返回操作数据条目数：1
            }
        }
        //取消点赞成功
        if(isSuccess==1){
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String blogLikeCountModifyKey = PrefixKeyConstant.BLOG_LIKE_COUNT_MODIFY_PREFIX + blogId;//博客点赞数变化数key

            Blog blog = blogMapper.selectById(blogId);
            String userLikeCountModifyKey = PrefixKeyConstant.USER_LIKE_COUNT_MODIFY_PREFIX + blog.getCreatorId();//用户总获得点赞数变化数key

            //博客点赞数变化数-1
            ops.decrement(blogLikeCountModifyKey);//自减，如果key不存在，则先创建整个key且值为0，而后再自减
            //被点赞博客的作者的总点赞数变化数-1
            ops.decrement(userLikeCountModifyKey);
        }
    }



    //删除点赞关系(批量)并返回删除已点赞关系数目
    @Override
    public int deleteLikeBatchByBlogId(String blogId) {
        QueryWrapper<BlogLikeUser> wrapper = new QueryWrapper<>();
        wrapper.eq("blog_id",blogId);
        wrapper.eq("status",1);
        int likeCount = baseMapper.delete(wrapper);

        wrapper = new QueryWrapper<>();
        wrapper.eq("blog_id",blogId);
        wrapper.eq("status",0);
        int delete = baseMapper.delete(wrapper);

        return likeCount;
    }

    //查看指定用户是否点赞指定博客
    @Override
    public boolean getIsLikeBlogByUserId(String userId, String blogId) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String blogLikeByUserKey = PrefixKeyConstant.BLOG_LIKE_BY_USER_PREFIX+userId+"-"+blogId;//用户点赞博客信息key
        String blogLikeByUserLockKey = PrefixKeyConstant.BLOG_LIKE_BY_USER_LOCK_PREFIX +userId+"-"+ blogId;//用户点赞博客信息锁key

        String isLike = ops.get(blogLikeByUserKey);//1为已点赞，0为未点赞
        if(isLike==null) {
            RLock lock = redissonClient.getLock(blogLikeByUserLockKey);
            lock.lock();//加锁，阻塞
            try {//双查机制，在锁内再查一遍缓存中是否有数据
                isLike = ops.get(blogLikeByUserKey);
                if (isLike == null) {
                    //查询点赞信息
                    QueryWrapper<BlogLikeUser> wrapper = new QueryWrapper<>();
                    wrapper.eq("user_id",userId);
                    wrapper.eq("blog_id",blogId);
                    wrapper.select("status");
                    BlogLikeUser blogLikeUser = baseMapper.selectOne(wrapper);
                    isLike= String.valueOf(blogLikeUser != null && blogLikeUser.getStatus());

                    //保存到redis中，并设置生存时长
                    ops.set(blogLikeByUserKey,isLike,
                            Constant.ONE_HOURS_EXPIRE + RandomSxpire.getRandomSxpire(), TimeUnit.MILLISECONDS);
                }
            } finally {
                lock.unlock();//解锁
            }
        }
        return isLike.equals("true");
    }
}
