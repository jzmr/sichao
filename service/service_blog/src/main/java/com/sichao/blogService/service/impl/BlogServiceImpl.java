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
            //带上博客的创建时间戳，用于之后保存进redis的话题下博客的key中
            LocalDateTime createTime = blog.getCreateTime();
            long createTimestamp = createTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();//转换成Unix时间戳
            topicMap.put("createTimestamp",createTimestamp);
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

    //查询指定话题id下的博客(使用redis的zSet数据类型缓存)
    @Override
    public List<BlogVo> getBlogByTopicId(String userId, String topicId,int page,int limit) {
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String blogZSetLockKey = PrefixKeyConstant.BLOG_BY_TOPIC_LOCK_PREFIX + topicId;//话题下综合博客id列表锁key
        String blogZSetKey = PrefixKeyConstant.BLOG_BY_TOPIC_PREFIX + topicId;//话题下综合博客id列表key
        String blogCommentCountModifyPrefix = PrefixKeyConstant.BLOG_COMMENT_COUNT_MODIFY_PREFIX;//博客评论数前缀
        String blogLikeCountModifyPrefix = PrefixKeyConstant.BLOG_LIKE_COUNT_MODIFY_PREFIX;//博客点赞数前缀

        //判断查询是否溢出
        Long size = zSet.size(blogZSetKey);//key不存在时，结果为0
        if(size !=null && size != 0 && ((page-1L)*limit >= size)){
            return null;//此时查询的条数超过总博客数，直接返回null
        }

        //TODO 更新逻辑不好，热度实时计算逻辑也不好
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
                    if(blogIdList!=null && !blogIdList.isEmpty()){
                        for (BlogTopicRelation relation : blogIdList) {
                            String blogId = relation.getBlogId();
                            BlogVo blogVo = getBLogVoInfo(blogId);
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
                            //为key设置生存时长
                            stringRedisTemplate.expire(blogZSetKey,
                                    Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),
                                    TimeUnit.MILLISECONDS);
                        }
                    }else {
                        //如果查询的数据为空，则向缓存中写入空串，并设置5分钟（短期）的过期时间（避免缓存穿透）
                        zSet.add(blogZSetKey,"",0);
                        //为key设置生存时长
                        stringRedisTemplate.expire(blogZSetKey,
                                Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),//5分钟
                                TimeUnit.MILLISECONDS);
                    }
                    set = zSet.reverseRange(blogZSetKey, (long) (page - 1) *limit, (long) page *limit-1);//查询回显
                }
            }finally {
                lock.unlock();//解锁
            }
        }

        //根据保存在set中的blogId查询出BlogVo对象,并添加评论数与点赞数的变化数后保存进list中
        if(set==null)return null;
        List<BlogVo> blogVoList=new ArrayList<>();
        for (String blogId : set) {
            //博客vo数据处理
            BlogVo blogVo = blogVoHandle(userId, blogId, ops, blogCommentCountModifyPrefix, blogLikeCountModifyPrefix);
            blogVoList.add(blogVo);
        }
        return blogVoList;
    }

    //查询指定话题id下的实时博客(使用redis的zSet数据类型缓存)
    /**使用redis的zSet类型做为存放实时博客的容器，规定为以时间戳为分值插入数据
     * start:>=0 本次要查询的博客从start位置从右往左查询     -2：说明用户第一次进入查询实时博客的页面   -1：说明所有博客已经查询出来了
     * 使用start控制要查询的数据，limit控制查询的长度
     */
    @Override
    public Map<String,Object> getRealTimeBlogByTopicId(String userId, String topicId,int start,int limit) {
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String realTimeBlogZSetLockKey = PrefixKeyConstant.BLOG_REAL_TIME_BY_TOPIC_LOCK_PREFIX + topicId;//话题下实时博客id查询锁key
        String realTimeBlogZSetKey = PrefixKeyConstant.BLOG_REAL_TIME_BY_TOPIC_PREFIX + topicId;//话题下实时博客id的key
        String blogCommentCountModifyPrefix = PrefixKeyConstant.BLOG_COMMENT_COUNT_MODIFY_PREFIX;//博客评论数前缀
        String blogLikeCountModifyPrefix = PrefixKeyConstant.BLOG_LIKE_COUNT_MODIFY_PREFIX;//博客点赞数前缀

        if(start==-1)return null;//所有博客已经查询出来了,直接返回null
        Set<String> set=null;
        Long size = zSet.size(realTimeBlogZSetKey);//key不存在时，结果为0
        if(size !=null && size >0){//key中有数据则进行处理
            //判断查询是否溢出
            if(start>=size)return null;//查询的条数超过总博客数
            else if(start>=0) set = zSet.range(realTimeBlogZSetKey, Math.max(start - limit + 1, 0), start);//以升序查询，从右往左拿里limit条数据
            else if(start==-2)set = zSet.range(realTimeBlogZSetKey, Math.max(size-limit,0),size-1);//以升序查询，从最右边开始往左拿limit条数据
        }//key中无数据则查询数据库

        if(set==null || set.isEmpty()){
            RLock lock = redissonClient.getLock(realTimeBlogZSetLockKey);
            lock.lock();//加锁，阻塞
            try{//双查机制，在锁内再查一遍缓存中是否有数据
                size = zSet.size(realTimeBlogZSetKey);//key不存在时，结果为0
                if(size !=null && size >0){//key中有数据则进行处理
                    //判断查询是否溢出
                    if(start>=size)return null;//查询的条数超过总博客数
                    else if(start>=0) set = zSet.range(realTimeBlogZSetKey, Math.max(start - limit + 1, 0), start);//以升序查询，从右往左拿里limit条数据
                    else if(start==-2)set = zSet.range(realTimeBlogZSetKey, Math.max(size-limit,0),size-1);//以升序查询，从最右边开始往左拿limit条数据
                }//key中无数据则查询数据库

                if(set==null || set.isEmpty()){
                    //查询话题下所有实时博客(根据创建时间升序查询)
                    List<BlogTopicRelation> blogIdList = blogTopicRelationService.getRealTimetBlogListByTopicId(topicId);
                    if(blogIdList!=null && !blogIdList.isEmpty()){
                        for (BlogTopicRelation relation : blogIdList) {
                            String blogId = relation.getBlogId();
                            //转换成Unix时间戳
                            LocalDateTime createTime = relation.getCreateTime();
                            long timestamp = createTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

                            //保存到redis中
                            zSet.add(realTimeBlogZSetKey,blogId,timestamp);
                        }
                        //为key设置生存时长
                        stringRedisTemplate.expire(realTimeBlogZSetKey,
                                Constant.THIRTY_DAYS_EXPIRE + RandomSxpire.getRandomSxpire(),//30天
                                TimeUnit.MILLISECONDS);
                    }else {
                        //如果查询的数据为空，则向缓存中写入空串，并设置5分钟（短期）的过期时间（避免缓存穿透）
                        zSet.add(realTimeBlogZSetKey,"",0);
                        //为key设置生存时长
                        stringRedisTemplate.expire(realTimeBlogZSetKey,
                                Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),//5分钟
                                TimeUnit.MILLISECONDS);
                    }

                    //查询缓存赋值给set给后面使用
                    size = zSet.size(realTimeBlogZSetKey);//key不存在时，结果为0
                    if(size !=null && size >0){//key中有数据则进行处理
                        //判断查询是否溢出
                        if(start>=size)return null;//查询的条数超过总博客数
                        else if(start>=0) set = zSet.range(realTimeBlogZSetKey, Math.max(start - limit + 1, 0), start);//以升序查询，从右往左拿里limit条数据
                        else if(start==-2)set = zSet.range(realTimeBlogZSetKey, Math.max(size-limit,0),size-1);//以升序查询，从最右边开始往左拿limit条数据
                    }
                }
            }finally {
                lock.unlock();//解锁
            }
        }

        //根据保存在集合中的blogId查询出BlogVo对象,并添加评论数与点赞数的变化数后保存进集合中
        if(set==null)return null;
        List<BlogVo> blogVoList=new ArrayList<>();
        int end=0;
        if(start>=0) end=Math.max(start - limit + 1, 0);
        else if(start==-2)end= (int) Math.max(size-limit,0);

        int len=0;//用来控制倒插数据到set时的索引
        //去获取十个数据，全拿满就不继续拿了
        while (set!=null && !set.isEmpty()){
            for (String blogId : set) {
                //博客vo数据处理
                BlogVo blogVo = blogVoHandle(userId, blogId, ops, blogCommentCountModifyPrefix, blogLikeCountModifyPrefix);
                blogVoList.add(len,blogVo);//倒插
                if(blogVoList.size()==limit)break;
            }
            len=blogVoList.size();
            if(len==limit || end==0){
                break;
            }
            int st=end-1;
            end=Math.max(end-limit,0);//重新计算末尾值索引
            set=zSet.range(realTimeBlogZSetKey,end,st);
        }
        Map<String,Object> map=new HashMap<>();
        map.put("blogVoList",blogVoList);
        map.put("end",end);
        return map;
    }

    //查询用户博客
    /**使用redis的zSet类型做为存放用户博客的容器，规定为以时间戳为分值插入数据
     * start:>=0 本次要查询的博客从start位置从右往左查询     -2：说明用户第一次进入查询实时博客的页面   -1：说明缓存中所有博客已经查询出来了 -3：数据中的博客数据也查询忘了
     * startTimestamp用来动态记录上一次从型数据库时，查询到哪个时间，将查询的数据的时间最小的时间转换成时间戳返回给前端，前端点击查询更多博客时带上该这个数据
     * 使用start控制要查询的数据，limit控制查询的长度
     */
    @Override
    public Map<String,Object> getUserBlog(String userId, String targetUserId, int start, int limit,long startTimestamp) {
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String userBlogZSetLockKey = PrefixKeyConstant.BLOG_USER_BLOG_LOCK_PREFIX + targetUserId;//用户的博客的id查询锁的key
        String userBlogZSetKey = PrefixKeyConstant.BLOG_USER_BLOG_PREFIX + targetUserId;//用户的博客id的key
        String blogCommentCountModifyPrefix = PrefixKeyConstant.BLOG_COMMENT_COUNT_MODIFY_PREFIX;//博客评论数前缀
        String blogLikeCountModifyPrefix = PrefixKeyConstant.BLOG_LIKE_COUNT_MODIFY_PREFIX;//博客点赞数前缀

        if(start==-3)return null;//数据以全部取完，直接返回null

        Set<String> set=new HashSet<>();
        Long size = 0L;
        if(start==-1){//表示缓存中的博客数据已被消费完，去数据库中拉取100条记录
            //获取最后一条博客的时间
            if(startTimestamp==0) {
                Set<ZSetOperations.TypedTuple<String>> typedTuples = zSet.rangeWithScores(userBlogZSetKey, 0, 0);
                if (typedTuples != null && !typedTuples.isEmpty()) {
                    for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
                        Double score = typedTuple.getScore();
                        startTimestamp = (long) score.doubleValue();
                    }
                }
            }
            LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(startTimestamp/1000,0,ZoneOffset.ofHours(0));
            //查询用户博客(根据创建时间倒序查询前100条)
            QueryWrapper<Blog> wrapper = new QueryWrapper<>();
            wrapper.eq("creator_id",targetUserId);
            wrapper.eq("status",1);
            wrapper.lt("create_time",localDateTime);//博客创建时间小于指定时间
            wrapper.select("id","create_time");
            wrapper.orderByDesc("create_time");
            wrapper.last("limit 100");
            List<Blog> blogList = baseMapper.selectList(wrapper);

            Map<String,Object> map=new HashMap<>();
            List<BlogVo> blogVoList=new ArrayList<>();
            if(blogList!=null && !blogList.isEmpty()){
                for (Blog blog : blogList) {
                    String blogId = blog.getId();
                    //博客vo数据处理
                    BlogVo blogVo = blogVoHandle(userId, blogId, ops, blogCommentCountModifyPrefix, blogLikeCountModifyPrefix);
                    blogVoList.add(blogVo);
                }
                //将最后一个元素（也就是创建时间最早的元素）的创建时间转换成Unix时间戳，而后返回给前端
                LocalDateTime createTime = blogList.get(blogList.size() - 1).getCreateTime();
                startTimestamp = createTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

                map.put("end",-1);
                map.put("startTimestamp",startTimestamp);
            }else {
                //表示数据以全部取完
                map.put("end",-3);
                map.put("startTimestamp",0);
                return map;
            }
            map.put("blogVoList",blogVoList);
            return map;
        }else {
            size = zSet.size(userBlogZSetKey);//key不存在时，结果为0
            if(size !=null && size >0){//key中有数据则进行处理
                //判断查询是否溢出
                if(start>=size)return null;//查询的条数超过总博客数
                else if(start>=0) set = zSet.range(userBlogZSetKey, Math.max(start - limit + 1, 0), start);//以升序查询，从右往左拿里limit条数据
                else if(start==-2)set = zSet.range(userBlogZSetKey, Math.max(size-limit,0),size-1);//以升序查询，从最右边开始往左拿limit条数据
            }
        }
        if(set==null || set.isEmpty()){
            RLock lock = redissonClient.getLock(userBlogZSetLockKey);
            lock.lock();//加锁，阻塞
            try {//双查机制，在锁内再查一遍缓存中是否有数据
                set = zSet.range(userBlogZSetKey, 0, 0);
                if(set==null || set.isEmpty()){
                    //查询用户博客(根据创建时间倒序查询前200条)
                    QueryWrapper<Blog> wrapper = new QueryWrapper<>();
                    wrapper.eq("creator_id",targetUserId);
                    wrapper.eq("status",1);
                    wrapper.select("id","create_time");
                    wrapper.orderByDesc("create_time");
                    wrapper.last("limit 200");
                    List<Blog> blogList = baseMapper.selectList(wrapper);

                    if(blogList!=null && !blogList.isEmpty()){
                        for (Blog blog : blogList) {
                            String blogId = blog.getId();
                            //转换成Unix时间戳
                            LocalDateTime createTime = blog.getCreateTime();
                            long timestamp = createTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

                            //保存到redis中
                            zSet.add(userBlogZSetKey,blogId,timestamp);
                        }
                        //为key设置生存时长
                        stringRedisTemplate.expire(userBlogZSetKey,
                                Constant.THREE_DAYS_EXPIRE + RandomSxpire.getRandomSxpire(),//3天
                                TimeUnit.MILLISECONDS);
                    }else {
                        //如果查询的数据为空，则向缓存中写入空串，并设置5分钟（短期）的过期时间（避免缓存穿透）
                        zSet.add(userBlogZSetKey,"",0);
                        //为key设置生存时长
                        stringRedisTemplate.expire(userBlogZSetKey,
                                Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),//5分钟
                                TimeUnit.MILLISECONDS);
                    }
                }
                //查询缓存赋值给set给后面使用
                size = zSet.size(userBlogZSetKey);//key不存在时，结果为0
                if(size !=null && size >0){//key中有数据则进行处理
                    //判断查询是否溢出
                    if(start>=size)return null;//查询的条数超过总博客数
                    else if(start>=0) set = zSet.range(userBlogZSetKey, Math.max(start - limit + 1, 0), start);//以升序查询，从右往左拿里limit条数据
                    else if(start==-2)set = zSet.range(userBlogZSetKey, Math.max(size-limit,0),size-1);//以升序查询，从最右边开始往左拿limit条数据
                }
            }finally {
                lock.unlock();//解锁
            }
        }

        //根据保存在集合中的blogId查询出BlogVo对象,并添加评论数与点赞数的变化数后保存进集合中
        if(set==null)return null;
        List<BlogVo> blogVoList=new ArrayList<>();
        for (String blogId : set) {
            //博客vo数据处理
            BlogVo blogVo = blogVoHandle(userId, blogId, ops, blogCommentCountModifyPrefix, blogLikeCountModifyPrefix);
            blogVoList.add(0,blogVo);//倒插
        }
        int end=0;
        if(start>=0) end=Math.max(start - limit + 1, 0);
        else if(start==-2)end= (int) Math.max(size-limit,0);
        Map<String,Object> map=new HashMap<>();
        map.put("blogVoList",blogVoList);
        map.put("end",end);
        map.put("startTimestamp",startTimestamp);
        return map;
    }

    //查询我的关注用户的博客(使用redis的zSet数据类型缓存)
    @Override
    public List<BlogVo> getMyFeedBlog(String userId,int start, int limit) {
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//规定为以时间戳为分值插入数据
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String feedBlogZSetLockKey = PrefixKeyConstant.BLOG_FEED_LOCK_PREFIX + userId;//关注用户的博客id查询锁key
        String feedBlogZSetKey = PrefixKeyConstant.BLOG_FEED_PREFIX + userId;//关注用户的博客id的key
        String blogCommentCountModifyPrefix = PrefixKeyConstant.BLOG_COMMENT_COUNT_MODIFY_PREFIX;//博客评论数前缀
        String blogLikeCountModifyPrefix = PrefixKeyConstant.BLOG_LIKE_COUNT_MODIFY_PREFIX;//博客点赞数前缀

        if(start==-1){//feed流已被消费完，在拉取100条记录到feed流收件箱中

        }else if(start==-2){

        }else {

        }

//        //查询该用户上一次在Feed流中获取博客数据的时间戳
//        Double score = zSet.score(feedTimestampZSetKey, userId);
//        zSet.range()
//        System.out.println("=============score:"+score);
//        if(score==null){//无指定用户上一次拉取feed流的时间戳，默认拉取100条记录
//
//        }else {
//
//        }


        return null;
    }

    //获取博客vo信息（使用redis的String类型缓存）
    public BlogVo getBLogVoInfo(String blogId){
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
                    if(blogVo == null){
                        //如果查询的数据为空，则向缓存中写入空串，并设置5分钟（短期）的过期时间（避免缓存穿透）
                        ops.set(blogVoInfoKey,"",
                                Constant.FIVE_MINUTES_EXPIRE + RandomSxpire.getMinRandomSxpire(),TimeUnit.MILLISECONDS);//5分钟
                    }else {
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
                }
            } finally {
                lock.unlock();//解锁
            }
        }

        return blogVo;
    }

    /**
     * 查询博客时的处理方法
     * @param userId 当前用户id
     * @param blogId 博客id
     * @param ops    redis的string类型处理对象
     * @param blogCommentCountModifyPrefix  评论数变化数前缀
     * @param blogLikeCountModifyPrefix     点赞数变化数前缀
     * @return BlogVo对象
     */
    public BlogVo blogVoHandle(String userId,String blogId,ValueOperations<String, String> ops,String blogCommentCountModifyPrefix,String blogLikeCountModifyPrefix){
        //为true则说明以不是正常的博客id，是已经被删除的为了避免顺序错乱而用来占位,或是防止缓存穿透的空串缓存
        if(blogId.contains(Constant.BLOG_DELETE_PREFIX) || "".equals(blogId)){
            return null;//此时不拿数据
        }

        BlogVo blogVo = getBLogVoInfo(blogId);
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
        return blogVo;
    }


}
