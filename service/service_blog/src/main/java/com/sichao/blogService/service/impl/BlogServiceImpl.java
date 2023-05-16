package com.sichao.blogService.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.client.UserClient;
import com.sichao.blogService.entity.Blog;
import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.entity.BlogTopicRelation;
import com.sichao.blogService.entity.vo.BlogVo;
import com.sichao.blogService.entity.vo.PublishBlogVo;
import com.sichao.blogService.mapper.BlogMapper;
import com.sichao.blogService.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.constant.RabbitMQConstant;
import com.sichao.common.entity.MqMessage;
import com.sichao.common.entity.to.UserInfoTo;
import com.sichao.common.exceptionhandler.sichaoException;
import com.sichao.common.mapper.MqMessageMapper;
import com.sichao.common.utils.R;
import com.sichao.common.utils.RandomSxpire;
import io.swagger.v3.core.util.Json;
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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 博客表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    @Autowired
    private BlogTopicService blogTopicService;
    @Autowired
    private UserClient userClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MqMessageMapper mqMessageMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private BlogLikeUserService blogLikeUserService;
    @Autowired
    private BlogTopicRelationService blogTopicRelationService;



    //发布博客
    @Transactional
    @Override
    public void saveBlog(PublishBlogVo publishBlogVo) {
        String content = publishBlogVo.getContent();//获取博客内容
        if(!StringUtils.hasText(content))
            throw new sichaoException(Constant.FAILURE_CODE,"博客内容不能为空");
        List<String> topicIdList=new ArrayList<>();//用来保存话题id的集合
        List<String> userIdList=new ArrayList<>();//用来保存用户id的集合
        StringBuilder strb=new StringBuilder();//用来拼接博客内容
        int idx = 0;

        /**
         * 1、话题title与用户昵称唯一的，且都是没有前导或后导空格的
         * 2、必须是先匹配@用户，再匹配#话题#，不然会有bug（ #s @sad# ）,拼接
         *    好@用户的前中缀之后的长度会大于25，是的#话题#的正则表达式不会生效
         */
        //使用正则表达式获取博客中被@的用户（@用户 ）
        //匹配以@开头，后面跟随着至少一个非'@'、非空格字符,最后匹配零个或一个空格
        Pattern referer_pattern_user = Pattern.compile("@([^@^\\s]{1,})([\\s]{0,1})");
        Matcher matchr_user = referer_pattern_user.matcher(content);
        //之前字符串中匹配到的位置不会在被匹配到，会往后开始匹配，配合while循环做到匹配整个字符串中所有符合正则表达式的子串
        while (matchr_user.find()){//为true说明匹配，为false说明不匹配
            String origion_str_user = matchr_user.group();//获取匹配到的字符串
            String userStr = origion_str_user.substring(1, origion_str_user.length()).trim();//裁剪

            //TODO 这里的循环查库优化？
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

        content=strb.toString();//重新赋值
        strb.delete(0,strb.length());//清空

        //使用正则表达式获取话题（ #话题# ）
        //正则表达式：^ {1}#{1}[^# ]{1,25}#{1} {1}$
        // 5~29个字符，第一个与最后一个字符必须是空格，第二个与倒数第二个字符必须是'#'，其余的字符只能是不能为'#',
        //下面的代码不能有^与$,加了会对整个字符串进行匹配，如果字符串不是以#号开头或结尾则不匹配
        //不加则可以用来对字符串中每个子串都进行匹配判断
        Pattern referer_pattern_topic = Pattern.compile(" {1}#{1}[^# ]{1,25}#{1} {1}");
        Matcher matchr_topic = referer_pattern_topic.matcher(content);
        idx=0;
        //之前字符串中匹配到的位置不会在被匹配到，会往后开始匹配，配合while循环做到匹配整个字符串中所有符合正则表达式的子串
        while (matchr_topic.find()){//为true说明匹配，为false说明不匹配。
            String origion_str_topic = matchr_topic.group();//获取匹配到的字符串
            String topicStr = origion_str_topic.substring(2, origion_str_topic.length()-2).trim();//裁剪

            //TODO 这里的循环查库优化？抽取变量？
            QueryWrapper<BlogTopic> wrapperTopic = new QueryWrapper<>();
            wrapperTopic.eq("topic_title",topicStr);
            BlogTopic blogTopic = blogTopicService.getOne(wrapperTopic);//查询话题id

            //给#话题#添加超链接
            //matchr.start()：获得被匹配到的子串在原串的起始位置
            strb.append(content.substring(idx, matchr_topic.start()));
            if(blogTopic!=null){
                topicIdList.add(blogTopic.getId());//添加话题id到集合
                strb.append(Constant.BLOG_AT_TOPIC_HYPERLINK_PREFIX)
                        .append(blogTopic.getId())
                        .append(Constant.BLOG_AT_TOPIC_HYPERLINK_INFIX);
            }
            strb.append(origion_str_topic);
            if(blogTopic!=null) {
                strb.append(Constant.BLOG_AT_TOPIC_HYPERLINK_SUFFIX);
            }

            idx=matchr_topic.start()+origion_str_topic.length();
        }
        strb.append(content.substring(idx));


        //保存博客
        Blog blog = new Blog();
        BeanUtils.copyProperties(publishBlogVo,blog);
        blog.setContent(strb.toString());//拼接了超链接的博客内容
        baseMapper.insert(blog);
        //用户博客数+1
        userClient.userBlogCountPlusOne(blog.getCreatorId());
        stringRedisTemplate.delete(PrefixKeyConstant.USER_INFO_PREFIX + blog.getCreatorId());//删除用户信息缓存，使得用户博客数可以立即更新之前数据

        //RabbitMQ发送消息，异步实现对@用户与#话题#的处理
        //博客中有#话题#时
        if(!topicIdList.isEmpty()){
            String blogId = blog.getId();//获取自动生成的id
            Map<String,Object> topicMap = new HashMap<>();
            topicMap.put("blogId",blogId);
            topicMap.put("topicIdList",topicIdList);
            //发送消息前先记录数据
            String topicMapJson = JSON.toJSONString(topicMap);
            MqMessage topicMqMessage = new MqMessage(topicMapJson,RabbitMQConstant.BLOG_EXCHANGE,RabbitMQConstant.BLOG_BINDING_TOPIC_ROUTINGKEY,
                    "Map<String,Object>",(byte)0);
            mqMessageMapper.insert(topicMqMessage);

            //指定路由，给交换机发送数据，并且携带数据标识
            rabbitTemplate.convertAndSend(RabbitMQConstant.BLOG_EXCHANGE,RabbitMQConstant.BLOG_BINDING_TOPIC_ROUTINGKEY,
                    topicMap,new CorrelationData(topicMqMessage.getId()));//以mq消息表id作为数据标识
        }
        //博客中@用户时
        if(!userIdList.isEmpty()){
            String blogId = blog.getId();//获取自动生成的id
            Map<String,Object> userMap=new HashMap<>();
            userMap.put("blogId",blogId);
            userMap.put("userIdList",userIdList);
            //发送消息前先记录数据
            String userMapJson = JSON.toJSONString(userMap);
            MqMessage UserMqMessage = new MqMessage(userMapJson,RabbitMQConstant.BLOG_EXCHANGE,RabbitMQConstant.BLOG_AT_USER_ROUTINGKEY,
                    "Map<String,Object>",(byte)0);
            mqMessageMapper.insert(UserMqMessage);

            //指定路由，给交换机发送数据，并且携带数据标识
            rabbitTemplate.convertAndSend(RabbitMQConstant.BLOG_EXCHANGE,RabbitMQConstant.BLOG_AT_USER_ROUTINGKEY,
                    userMap,new CorrelationData(UserMqMessage.getId()));//以mq消息表id作为数据标识

        }

    }

    //删除博客及其下的所有评论，以及点赞关系、话题关系、并自减各个数据
    @Transactional
    @Override
    public void deleteBlog(String userId, String blogId) {
        //先查看博客是否存在且当前用户是该博客的作者，是则可以删除，不是则不能做删除操作
        Blog blog = baseMapper.selectById(blogId);
        if(blog==null || !blog.getCreatorId().equals(userId)){
            throw new sichaoException(Constant.FAILURE_CODE,"删除博客异常，博客不存在或者当前用户不是该博客作者");
        }
        //删除博客
        baseMapper.deleteById(blogId);

        //RabbitMQ异步处理删除博客之后的操作
        Map<String,Object> map = new HashMap<>();
        map.put("blogId",blogId);
        map.put("userId",userId);
        //发送消息前先记录数据
        String topicMapJson = JSON.toJSONString(map);
        MqMessage topicMqMessage = new MqMessage(topicMapJson,RabbitMQConstant.BLOG_EXCHANGE,RabbitMQConstant.BLOG_DELETE_ROUTINGKEY,
                "Map<String,Object>",(byte)0);
        mqMessageMapper.insert(topicMqMessage);

        //指定路由，给交换机发送数据，并且携带数据标识
        rabbitTemplate.convertAndSend(RabbitMQConstant.BLOG_EXCHANGE,RabbitMQConstant.BLOG_DELETE_ROUTINGKEY,
                map,new CorrelationData(topicMqMessage.getId()));//以mq消息表id作为数据标识
    }

    //查询指定话题id下的博客(使用redis的zSet数据类型缓存)(前端会根据BlogVo的id属性去除重复数据)
    @Override
    public List<BlogVo> getBlogByTopicId(String userId, String topicId,int page,int limit) {
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String blogZSetLockKey = PrefixKeyConstant.BLOG_BY_TOPIC_LOCK_PREFIX + topicId;//话题下综合博客列表锁key
        String blogZSetKey = PrefixKeyConstant.BLOG_BY_TOPIC_PREFIX + topicId;//话题下综合博客列表key
        String blogCommentCountModifyPrefix = PrefixKeyConstant.BLOG_COMMENT_COUNT_MODIFY_PREFIX;//博客评论数前缀
        String blogLikeCountModifyPrefix = PrefixKeyConstant.BLOG_LIKE_COUNT_MODIFY_PREFIX;//博客点赞数前缀

        //判断查询是否溢出
        Long size = zSet.size(blogZSetKey);//key不存在时，结果为0
        if(size !=null && size != 0 && ((page-1L)*limit >= size)){
            return null;//此时查询的条数超过总博客数，直接返回null
        }

        //根据分值降序分页查询指定的博客//如果无这个key，则这句代码查询到的会使一个空的set集合
        Set<String> set = zSet.reverseRange(blogZSetKey, (long) (page - 1) *limit, (long) page *limit-1);
        if(set==null || set.isEmpty()){
            RLock lock = redissonClient.getLock(blogZSetLockKey);
            lock.lock();//加锁，阻塞
            try{//双查机制，在锁内再查一遍缓存中是否有数据
                set = zSet.reverseRange(blogZSetKey, (long) (page - 1) *limit, (long) page *limit-1);
                if(set==null || set.isEmpty()){
                    //查询话题下所有博客
                    List<BlogTopicRelation> blogIdList = blogTopicRelationService.getBlogListByTopicId(topicId);

                    for (BlogTopicRelation relation : blogIdList) {
                        String blogId = relation.getBlogId();
                        BlogVo blogVo = getBLogVoInfo(userId,blogId);
                        if(blogVo==null)continue;

                        //计算分值//分值等于博客下评论数+点赞数
                        int score=blogVo.getCommentCount()+blogVo.getLikeCount();
                        String CommentCountModify = ops.get(blogCommentCountModifyPrefix + blogVo.getId());
                        if(CommentCountModify!=null){
                            score += Integer.parseInt(CommentCountModify);
                        }
                        String likeCountModify = ops.get(blogLikeCountModifyPrefix + blogVo.getId());
                        if(likeCountModify != null){
                            score += Integer.parseInt(likeCountModify);
                        }
                        //保存到redis的zSet中
                        zSet.add(blogZSetKey,blogId,score);
                    }
                    //为key设置生存时长
                    stringRedisTemplate.expire(blogZSetKey,
                            Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),
                            TimeUnit.MILLISECONDS);
                }
            }finally {
                set = zSet.reverseRange(blogZSetKey, (long) (page - 1) *limit, (long) page *limit-1);//查询回显
                lock.unlock();//解锁
            }
        }

        //根据保存在set中的blogId查询出BlogVo对象,并添加评论数与点赞数的变化数后保存进list中
        if(set==null)return null;
        List<BlogVo> blogVoList=new ArrayList<>();
        for (String blogId : set) {
            BlogVo blogVo = getBLogVoInfo(userId, blogId);

            //博客信息可以查缓存，但是博客的评论数与点赞数要加上redis中的变化数
            String CommentCountModify = ops.get(blogCommentCountModifyPrefix + blogId);
            if(CommentCountModify!=null){
                blogVo.setCommentCount(blogVo.getCommentCount() + Integer.parseInt(CommentCountModify));
            }
            String likeCountModify = ops.get(blogLikeCountModifyPrefix + blogId);
            if(likeCountModify != null){
                blogVo.setLikeCount(blogVo.getLikeCount()+Integer.parseInt(likeCountModify));
            }
            //查看当前用户是否点赞该博客
            boolean likeByCurrentUser = blogLikeUserService.getIsLikeBlogByUserId(userId,blogId);
            blogVo.setLikeByCurrentUser(likeByCurrentUser);

            blogVoList.add(blogVo);
        }
        return blogVoList;
    }

    //查询指定话题id下的实时博客(使用redis的list数据类型缓存)(前端会根据BlogVo的id属性去除重复数据)
    @Override
    public List<BlogVo> getRealTimeBlogByTopicId(String userId, String topicId,int page,int limit) {
        ListOperations<String, String> forList = stringRedisTemplate.opsForList();//规定为左侧插入时间新的数据，右侧插入时间旧的数据
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String realTimeBlogListLockKey = PrefixKeyConstant.BLOG_REAL_TIME_BY_TOPIC_LOCK_PREFIX + topicId;//话题下实时博客查询锁key
        String realTimeBlogListKey = PrefixKeyConstant.BLOG_REAL_TIME_BY_TOPIC_PREFIX + topicId;//话题下实时博客key
        String blogCommentCountModifyPrefix = PrefixKeyConstant.BLOG_COMMENT_COUNT_MODIFY_PREFIX;//博客评论数前缀
        String blogLikeCountModifyPrefix = PrefixKeyConstant.BLOG_LIKE_COUNT_MODIFY_PREFIX;//博客点赞数前缀

        //判断查询是否溢出
        Long size = forList.size(realTimeBlogListKey);//key不存在时，结果为0
        if(size !=null && size != 0 && ((page-1L)*limit >= size)){
            return null;//此时查询的条数超过总博客数，直接返回null
        }

        List<String> list = forList.range(realTimeBlogListKey, (long) (page - 1) * limit, (long) page * limit - 1);
        if(list==null || list.isEmpty()){
            RLock lock = redissonClient.getLock(realTimeBlogListLockKey);
            lock.lock();//加锁，阻塞
            try{//双查机制，在锁内再查一遍缓存中是否有数据
                list = forList.range(realTimeBlogListKey, (long) (page - 1) * limit, (long) page * limit - 1);
                if(list==null || list.isEmpty()){
                    //查询话题下所有实时博客(根据创建时间倒序查询)
                    List<BlogTopicRelation> blogIdList = blogTopicRelationService.geRealTimetBlogListByTopicId(topicId);
                    for (BlogTopicRelation relation : blogIdList) {
                        String blogId = relation.getBlogId();
                        BlogVo blogVo = getBLogVoInfo(userId,blogId);//查询博客信息
                        if(blogVo==null)continue;

                        //保存到redis的list中
                        forList.rightPush(realTimeBlogListKey,blogId);
                    }
                    //为key设置生存时长
                    stringRedisTemplate.expire(realTimeBlogListKey,
                            Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),
                            TimeUnit.MILLISECONDS);
                }
            }finally {
                list = forList.range(realTimeBlogListKey, (long) (page - 1) * limit, (long) page * limit - 1);
                lock.unlock();//解锁
            }
        }

        //根据保存在set中的blogId查询出BlogVo对象,并添加评论数与点赞数的变化数后保存进list中
        if(list==null)return null;
        List<BlogVo> blogVoList=new ArrayList<>();
        for (String blogId : list) {
            BlogVo blogVo = getBLogVoInfo(userId, blogId);

            //博客信息可以查缓存，但是博客的评论数与点赞数要加上redis中的变化数
            String CommentCountModify = ops.get(blogCommentCountModifyPrefix + blogId);
            if(CommentCountModify!=null){
                blogVo.setCommentCount(blogVo.getCommentCount() + Integer.parseInt(CommentCountModify));
            }
            String likeCountModify = ops.get(blogLikeCountModifyPrefix + blogId);
            if(likeCountModify != null){
                blogVo.setLikeCount(blogVo.getLikeCount()+Integer.parseInt(likeCountModify));
            }
            //查看当前用户是否点赞该博客
            boolean likeByCurrentUser = blogLikeUserService.getIsLikeBlogByUserId(userId,blogId);
            blogVo.setLikeByCurrentUser(likeByCurrentUser);

            blogVoList.add(blogVo);
        }
        return blogVoList;
    }

    //获取博客vo信息（使用redis的String类型缓存，只所以不会用hash类型是因为无法给hash类型的key中的单个field字段设置生存时长）
    public BlogVo getBLogVoInfo(String userId,String blogId){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String blogVoInfoKey = PrefixKeyConstant.BLOG_VO_INFO_PREFIX+blogId;//博客信息key
        String blogVoInfoLockKey = PrefixKeyConstant.BLOG_VO_INFO_LOCK_PREFIX + blogId;//博客信息锁key

        BlogVo blogVo = JSON.parseObject(ops.get(blogVoInfoKey),BlogVo.class);
        if(blogVo==null) {
            RLock lock = redissonClient.getLock(blogVoInfoLockKey);
            lock.lock();//加锁，阻塞
            try {//双查机制，在锁内再查一遍缓存中是否有数据
                blogVo = JSON.parseObject(ops.get(blogVoInfoKey),BlogVo.class);
                if (blogVo == null) {
                    //查询博客vo信息
                    blogVo = baseMapper.getBlogVoInfo(blogId);
                    if(blogVo == null)return null;
                    List<String> imgList = new ArrayList<>();
                    if (blogVo.getImgOne() != null) imgList.add(blogVo.getImgOne());
                    if (blogVo.getImgTwo() != null) imgList.add(blogVo.getImgTwo());
                    if (blogVo.getImgThree() != null) imgList.add(blogVo.getImgThree());
                    if (blogVo.getImgFour() != null) imgList.add(blogVo.getImgFour());
                    blogVo.setImgList(imgList);
                    //保存到redis中，并设置生存时长
                    ops.set(blogVoInfoKey,JSON.toJSONString(blogVo),
                            Constant.ONE_HOURS_EXPIRE + RandomSxpire.getRandomSxpire(),TimeUnit.MILLISECONDS);
                }
            } finally {
                lock.unlock();//解锁
            }
        }

        return blogVo;
    }


}
