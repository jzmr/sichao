package com.sichao.blogService.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.client.UserClient;
import com.sichao.blogService.entity.Blog;
import com.sichao.blogService.entity.BlogComment;
import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.entity.BlogTopicRelation;
import com.sichao.blogService.entity.vo.BlogVo;
import com.sichao.blogService.entity.vo.CommentVo;
import com.sichao.blogService.mapper.BlogCommentMapper;
import com.sichao.blogService.service.BlogCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.blogService.service.BlogService;
import com.sichao.blogService.service.BlogTopicRelationService;
import com.sichao.blogService.service.BlogTopicService;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.constant.RabbitMQConstant;
import com.sichao.common.entity.MqMessage;
import com.sichao.common.entity.to.UserInfoTo;
import com.sichao.common.exceptionhandler.sichaoException;
import com.sichao.common.mapper.MqMessageMapper;
import com.sichao.common.utils.R;
import com.sichao.common.utils.RandomSxpire;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 评论与子评论表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogCommentServiceImpl extends ServiceImpl<BlogCommentMapper, BlogComment> implements BlogCommentService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private MqMessageMapper mqMessageMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private BlogTopicRelationService blogTopicRelationService;
    @Autowired
    private BlogService blogService;

    //发布评论(博客下评论、父评论)
    @Transactional
    @Override
    public void saveComment(String curUserId, String blogId, String content) {
        //查看博客是否存在
        Blog blog = blogService.getById(blogId);
        if(blog==null){
            throw new sichaoException(Constant.FAILURE_CODE,"发布评论异常，博客不存在");
        }

        if(!StringUtils.hasText(content))
            throw new sichaoException(Constant.FAILURE_CODE,"评论内容不能为空");
        List<String> userIdList=new ArrayList<>();//用来保存用户id的集合
        StringBuilder strb=new StringBuilder();//用来拼接评论内容
        int idx = 0;

        //评论只偶有@用户功能，没有#话题#功能
        //使用正则表达式获取评论中被@的用户（@用户 ）
        //匹配以@开头，后面跟随着至少一个非'@'、非空格字符,最后匹配零个或一个空格
        Pattern referer_pattern_user = Pattern.compile("@([^@^\\s]{1,})([\\s]{0,1})");
        Matcher matchr_user = referer_pattern_user.matcher(content);
        //之前字符串中匹配到的位置不会在被匹配到，会往后开始匹配，配合while循环做到匹配整个字符串中所有符合正则表达式的子串
        while (matchr_user.find()){//为true说明匹配，为false说明不匹配
            String origion_str_user = matchr_user.group();//获取匹配到的字符串
            String userStr = origion_str_user.substring(1, origion_str_user.length()).trim();//裁剪

            R r = (R) userClient.getUserIdByNickname(userStr);//远程调用查询用户id
            String userId = (String) r.getData().get("userId");

            //给@用户添加超链接
            //matchr.start()：获得被匹配到的子串在原串的起始位置
            strb.append(content.substring(idx, matchr_user.start()));
            if(userId!=null){
                userIdList.add(userId);//添加用户id到集合
                strb.append(Constant.BLOG_AT_USER_HYPERLINK_PREFIX)
                        .append(userId)
                        .append(Constant.BLOG_AT_USER_HYPERLINK_INFIX);
            }
            strb.append('@'+userStr);
            if(userId!=null){
                strb.append(Constant.BLOG_AT_USER_HYPERLINK_SUFFIX);
            }
            idx=matchr_user.start()+userStr.length()+1;
        }
        strb.append(content.substring(idx));

        //保存评论
        BlogComment blogComment = new BlogComment();
        blogComment.setCreatorId(curUserId);
        blogComment.setBlogId(blogId);
        blogComment.setCommentContent(strb.toString());//拼接了超链接的评论内容
        baseMapper.insert(blogComment);


        //RabbitMQ发送消息，异步实现对@用户的处理
        if(!userIdList.isEmpty()){
            String blogCommentId = blogComment.getId();//获取自动生成的id
            //获取评论作者昵称
            R r = userClient.getUserById(blogComment.getCreatorId());
            String jsonString = JSON.toJSONString(r.getData().get("userInfoTo"));
            UserInfoTo userInfoTo = JSON.parseObject(jsonString, UserInfoTo.class);
            String commentCreatorNickname = userInfoTo.getNickname();
            String commentContent = blogComment.getCommentContent();

            //构建map
            Map<String,Object> userMap=new HashMap<>();
            userMap.put("blogCommentId",blogCommentId);
            userMap.put("blogId",blogId);
            userMap.put("commentContent",commentContent);
            userMap.put("commentCreatorId",blogComment.getCreatorId());
            userMap.put("commentCreatorNickname",commentCreatorNickname);
            userMap.put("userIdList",userIdList);
            //发送消息前先记录数据
            String userMapJson = JSON.toJSONString(userMap);
            MqMessage UserMqMessage = new MqMessage(userMapJson,RabbitMQConstant.BLOG_EXCHANGE,RabbitMQConstant.BLOG_COMMENT_AT_USER_ROUTINGKEY,
                    "Map<String,Object>",(byte)0);
            mqMessageMapper.insert(UserMqMessage);

            //指定路由，给交换机发送数据，并且携带数据标识
            rabbitTemplate.convertAndSend(RabbitMQConstant.BLOG_EXCHANGE,RabbitMQConstant.BLOG_COMMENT_AT_USER_ROUTINGKEY,
                    userMap,new CorrelationData(UserMqMessage.getId()));//以mq消息表id作为数据标识

        }

        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        //给redis中博客评论数变化数+1
        String commentCountModifyKey = PrefixKeyConstant.BLOG_COMMENT_COUNT_MODIFY_PREFIX + blogId;
        ops.increment(commentCountModifyKey);//自增，如果key不存在，则先创建整个key且值为0，而后再自增
        //话题中讨论数+1
        List<String> topicIdList=blogTopicRelationService.getTopicIdByBlogId(blogId);
        for (String topicId : topicIdList) {
            ops.increment(PrefixKeyConstant.BLOG_TOPIC_DISCUSSION_MODIFY_PREFIX+topicId);//自增
        }
        //将评论以创建时间的时间戳插入博客下评论key中
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        String commentZSetKey = PrefixKeyConstant.BLOG_COMMENT_PREFIX + blogId;//博客下评论key
        Long size = zSet.size(commentZSetKey);
        if(size!=null && size>0){
            LocalDateTime createTime = blogComment.getCreateTime();
            long createTimestamp = createTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();//转换成Unix时间戳
            zSet.add(commentZSetKey,blogComment.getId(),createTimestamp);//将评论id保存进博客下评论key中
        }
    }

    //删除评论、博客评论数-1
    @Override
    public void deleteComment(String userId, String commentId) {
        //先查看评论是否存在且当前用户是该评论的作者，是则可以删除，不是则不能做删除操作
        BlogComment blogComment = baseMapper.selectById(commentId);
        if(blogComment==null || !blogComment.getCreatorId().equals(userId)){
            throw new sichaoException(Constant.FAILURE_CODE,"删除评论异常，评论不存在或者当前用户不是该评论作者");
        }
        //删除评论
        baseMapper.deleteById(commentId);

        /**
         * 删除博客或评论时，不能直接删除排序列表中的value，不然会改变zSet的结果，出现查询出重复数据问题。
         * 需要先查询该value的分值score，再删除该value，在以delete+value为值、score为分值保存数据到zSet中，
         * 查询时如果id是delete开头的不去查询数据
         */
        //为该评论在所属博客下评论key中的数据添加Delete前缀
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        String commentZSetKey = PrefixKeyConstant.BLOG_COMMENT_PREFIX + blogComment.getBlogId();//博客下评论key
        Long size = zSet.size(commentZSetKey);//查看key的长度，key不存在时为0（key不存在时查看key的长度不会创建该key，即长度为0时该key不存在）
        if(size!=null && size>0){//key存在
            Double score = zSet.score(commentZSetKey, commentId);//获取分值
            if(score!=null) {
                zSet.remove(commentZSetKey, commentId);//移除评论id
                zSet.add(commentZSetKey, Constant.BLOG_DELETE_PREFIX + commentId, score);//以score为分值插入value占位
            }
        }

        //删除缓存评论信息key
        String commentVoInfoKey = PrefixKeyConstant.BLOG_COMMENT_VO_INFO_PREFIX+commentId;//评论vo信息key
        stringRedisTemplate.delete(commentVoInfoKey);

        //博客评论数-1
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.decrement(PrefixKeyConstant.BLOG_COMMENT_COUNT_MODIFY_PREFIX+blogComment.getBlogId());
    }


    //查询指定博客id下的评论(升序)(使用redis的zSet数据类型缓存)
    @Override
    public Map<String,Object> getCommentByBlogId(String blogId,int start,int limit) {
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        String commentZSetLockKey = PrefixKeyConstant.BLOG_COMMENT_LOCK_PREFIX + blogId;//博客下评论id查询锁key
        String commentZSetKey = PrefixKeyConstant.BLOG_COMMENT_PREFIX + blogId;//博客下评论id的key

        Set<String> set=null;
        //判断查询是否溢出
        Long size = zSet.size(commentZSetKey);//key不存在时，结果为0
        if(size !=null && size > 0){
            if(start >= size){
                return null;//此时查询的条数超过总评论数，直接返回null
            }else {
                //以升序查询，根据从start开始从左往右查询指定的博客//如果无这个key，则这句代码查询到的会使一个空的集合
                set = zSet.range(commentZSetKey, start, Math.min(start+limit-1,size-1));
            }
        }
        if(set==null || set.isEmpty()){
            RLock lock = redissonClient.getLock(commentZSetLockKey);
            lock.lock();//加锁，阻塞
            try{
                //根据博客id升序查询评论并加入缓存
                getCommentByBlogIdCache(blogId);
                //查询缓存赋值给set给后面使用
                size = zSet.size(commentZSetKey);
                set = zSet.range(commentZSetKey, start, Math.min(start+limit-1,size-1));
            }finally {
                lock.unlock();//解锁
            }
        }

        if(set==null)return null;
        List<CommentVo> commentVoList=new ArrayList<>();
        for (String commentId : set) {
            //为true则说明以不是正常的博客id，是已经被删除的为了避免顺序错乱而用来占位,或是防止缓存穿透的空串缓存
            if(commentId.contains(Constant.BLOG_DELETE_PREFIX) || "".equals(commentId)){
                continue;//此时不拿数据
            }
            CommentVo commentVo = getCommentVoInfo(commentId);
            commentVoList.add(commentVo);
        }
        Map<String,Object> map=new HashMap<>();
        map.put("commentList",commentVoList);
        map.put("end",Math.min(start+limit-1,size-1));
        return map;
    }

    //查询指定博客id下的评论（倒序）
    /**使用redis的zSet类型做为存放实时评论的容器，规定为以时间戳为分值插入数据
     * start:>=0 本次要查询的评论从start位置从右往左查询     -2：说明用户第一次进入查询降序实时评论的页面   -1：说明所有评论已经查询出来了
     * 使用start控制要查询的数据，limit控制查询的长度
     */
    @Override
    public Map<String,Object> getCommentByBlogIdDesc(String blogId,int start,int limit) {
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        String commentZSetLockKey = PrefixKeyConstant.BLOG_COMMENT_LOCK_PREFIX + blogId;//博客下评论id查询锁key
        String commentZSetKey = PrefixKeyConstant.BLOG_COMMENT_PREFIX + blogId;//博客下评论id的key

        if(start==-1)return null;//所有评论已经查询出来了,直接返回null
        Set<String> set=null;
        Long size = zSet.size(commentZSetKey);//key不存在时，结果为0
        if(size !=null && size >0){//key中有数据则进行处理
            //判断查询是否溢出
            if(start>=size)return null;//此时查询的条数超过总评论数，直接返回null
            else if(start>=0) set = zSet.range(commentZSetKey,Math.max(start-limit+1,0),start);//以升序查询，从右往左拿里limit条数据
            else if(start==-2)set = zSet.range(commentZSetKey, Math.max(size-limit,0),size-1);//以升序查询，从最右边开始往左拿limit条数据
        }//key中无数据则查询数据库

        if(set==null || set.isEmpty()){
            RLock lock = redissonClient.getLock(commentZSetLockKey);
            lock.lock();//加锁，阻塞
            try{
                //根据博客id升序查询评论并加入缓存
                getCommentByBlogIdCache(blogId);
                //查询缓存赋值给set给后面使用
                size = zSet.size(commentZSetKey);
                if(size !=null && size >0){//key中有数据则进行处理
                    //判断查询是否溢出
                    if(start>=size)return null;//此时查询的条数超过总评论数，直接返回null
                    else if(start>=0) set = zSet.range(commentZSetKey,Math.max(start-limit+1,0),start);//以升序查询，从右往左拿里limit条数据
                    else if(start==-2)set = zSet.range(commentZSetKey, Math.max(size-limit,0),size-1);//以升序查询，从最右边开始往左拿limit条数据
                }
            }finally {
                lock.unlock();//解锁
            }
        }

        if(set==null)return null;
        List<CommentVo> commentVoList=new ArrayList<>();
        for (String commentId : set) {
            //为true则说明以不是正常的博客id，是已经被删除的为了避免顺序错乱而用来占位,或是防止缓存穿透的空串缓存
            if(commentId.contains(Constant.BLOG_DELETE_PREFIX) || "".equals(commentId)){
                continue;//此时不拿数据
            }
            CommentVo commentVo = getCommentVoInfo(commentId);
            commentVoList.add(0,commentVo);//倒插
        }
        int end=0;
        if(start>=0) end=Math.max(start - limit + 1, 0);
        else if(start==-2)end= (int) Math.max(size-limit,0);

        Map<String,Object> map=new HashMap<>();
        map.put("commentList",commentVoList);
        map.put("end",end);
        return map;
    }

    //根据博客id升序查询评论并加入缓存
    public void getCommentByBlogIdCache(String blogId){
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        String commentZSetKey = PrefixKeyConstant.BLOG_COMMENT_PREFIX + blogId;//博客下评论id的key

        //双查机制，在锁内再查一遍缓存中是否有数据
        Set<String> set = zSet.range(commentZSetKey, 0, 0);
        if(set==null || set.isEmpty()){//缓存不存在
            //查询博客下所有实时评论(根据创建时间升序查询)
            List<CommentVo> commentVoList = baseMapper.getCommentByBlogId(blogId);
            if(commentVoList!=null && !commentVoList.isEmpty()) {
                for (CommentVo commentVo : commentVoList) {
                    //转换成Unix时间戳
                    LocalDateTime createTime = commentVo.getCreateTime();
                    long timestamp = createTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

                    //将评论id保存到redis中的zSet类型中，使用时间戳自动排序
                    zSet.add(commentZSetKey, commentVo.getId(), timestamp);
                }
                //为zSet的key设置生存时长
                stringRedisTemplate.expire(commentZSetKey,
                        Constant.THIRTY_DAYS_EXPIRE + RandomSxpire.getRandomSxpire(),//30天
                        TimeUnit.MILLISECONDS);
            }else {
                //如果查询的数据为空，则向缓存中写入空串，并设置5分钟（短期）的过期时间（避免缓存穿透）
                zSet.add(commentZSetKey,"",0);
                //为key设置生存时长
                stringRedisTemplate.expire(commentZSetKey,
                        Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),//5分钟
                        TimeUnit.MILLISECONDS);
            }
        }
    }
    //获取评论vo信息（使用redis的String类型缓存）
    public CommentVo getCommentVoInfo(String commentId){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String commentVoInfoKey = PrefixKeyConstant.BLOG_COMMENT_VO_INFO_PREFIX+commentId;//评论vo信息key
        String commentVoInfoLockKey = PrefixKeyConstant.BLOG_COMMENT_VO_INFO_LOCK_PREFIX + commentId;//评论vo信息锁key

        CommentVo commentVo = JSON.parseObject(ops.get(commentVoInfoKey), CommentVo.class);
        if(commentVo==null) {
            RLock lock = redissonClient.getLock(commentVoInfoLockKey);
            lock.lock();//加锁，阻塞
            try {//双查机制，在锁内再查一遍缓存中是否有数据
                commentVo = JSON.parseObject(ops.get(commentVoInfoKey), CommentVo.class);
                if (commentVo == null) {
                    //根据评论id查询评论vo信息
                    commentVo = baseMapper.getCommentVoInfo(commentId);
                    if(commentVo == null){
                        //如果查询的数据为空，则向缓存中写入空串，并设置5分钟（短期）的过期时间（避免缓存穿透）
                        ops.set(commentVoInfoKey,"",
                                Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),TimeUnit.MILLISECONDS);//5分钟
                    }else {
                        //保存到redis中，并设置生存时长
                        ops.set(commentVoInfoKey,JSON.toJSONString(commentVo),
                                Constant.ONE_HOURS_EXPIRE + RandomSxpire.getRandomSxpire(),TimeUnit.MILLISECONDS);//1小时
                    }
                }
            } finally {
                lock.unlock();//解锁
            }
        }

        return commentVo;
    }

    //删除评论(批量)
    @Transactional
    @Override
    public void deleteCommentBatchByBlogId(String blogId) {
        QueryWrapper<BlogComment> wrapper = new QueryWrapper<>();
        wrapper.eq("blog_id",blogId);
        baseMapper.delete(wrapper);
        //缓存中的评论信息这里就不去删除了，因为博客删除，其下的使用评论也不会被查询到了，
        //也就不会有查询重复数据的问题了。所以等待评论信息的生存时长过期就会自动删除。
    }
}
