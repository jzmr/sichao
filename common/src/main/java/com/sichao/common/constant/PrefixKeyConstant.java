package com.sichao.common.constant;

/**
 * @Description: 前缀key常量类,常用redis的key前缀
 * @author: sjc
 * @createTime: 2023年04月29日 21:53
 */
public class PrefixKeyConstant {//项目名:业务名:类型:id

    public static final String SMS_CODE_PREFIX = "sichao:sms:code:";//+手机号

    //用户模块
    public static final String USER_TOKEN_PREFIX = "sichao:user:token:";//+token值
    public static final String USER_BLACK_TOKEN_PREFIX = "sichao:user:blackToken:";//+token值    （黑名单Token前缀）

    public static final String USER_CRON_TASK_LOCK_PREFIX = "sichao:user:cronTaskLcok:";//+具体定时任务名  （用户模块定时任务锁）
    public static final String USER_INFO_PREFIX = "sichao:user:info:";//+用户id   （用户信息）
    public static final String USER_INFO_LOCK_PREFIX = "sichao:user:infoLock:";//+用户id   （用户信息锁）
    public static final String USER_FOLLOWER_MODIFY_PREFIX = "sichao:user:followerModify:";//+用户id  （用户粉丝数变化）
    public static final String USER_FOLLOWING_MODIFY_PREFIX = "sichao:user:followingModify:";//+用户id  （用户关注数变化）
    public static final String USER_LIKE_COUNT_MODIFY_PREFIX = "sichao:user:likeCountModify:";//+用户id  （用户总获得点赞数变化数）


    //博客模块
    public static final String BLOG_HOT_TOPIC_KEY = "sichao:blog:hotTopic";//     （热搜话题榜）
    public static final String BLOG_HOT_TOPIC_TEMP_KEY = "sichao:blog:hotTopicTemp";//     （临时热搜话题榜，用来缓存实时热度计算，全部计算完后合并到热搜榜中，避免因为热搜排行更新期间用户无法查询热搜）
    public static final String BLOG_CRON_TASK_LOCK_PREFIX = "sichao:blog:cronTaskLcok:";//+具体定时任务名  （博客模块定时任务锁）
    public static final String BLOG_TOPIC_DISCUSSION_MODIFY_PREFIX = "sichao:blog:topicDiscussionModify:";//+话题id  （话题讨论数变化）
    public static final String BLOG_COMMENT_COUNT_MODIFY_PREFIX = "sichao:blog:commentCountModify:";//+博客id     （博客评论数变化）
    public static final String BLOG_LIKE_COUNT_MODIFY_PREFIX = "sichao:blog:likeCountModify:";//+博客id     （博客点赞数变化）

    public static final String BLOG_VO_INFO_PREFIX = "sichao:blog:voInfo:";//+博客id     （博客vo信息）
    public static final String BLOG_VO_INFO_LOCK_PREFIX = "sichao:blog:VoInfoLock:";//+博客id   （博客vo信息锁）
    public static final String BLOG_LIKE_BY_USER_PREFIX = "sichao:blog:likeByUser:";//+用户id-博客id     （用户点赞博客信息）
    public static final String BLOG_LIKE_BY_USER_LOCK_PREFIX = "sichao:blog:likeByUserLock:";//+用户id-博客id   （用户点赞博客信息锁）

    public static final String BLOG_BY_TOPIC_LOCK_PREFIX = "sichao:blog:byTopicLock:";//+话题id   （话题下综合博客查询锁）
    public static final String BLOG_BY_TOPIC_PREFIX = "sichao:blog:byTopic:";//+话题id     （话题下综合博客）

    public static final String BLOG_REAL_TIME_BY_TOPIC_LOCK_PREFIX = "sichao:blog:realTimeByTopicLock:";//+话题id   （话题下实时博客查询锁）
    public static final String BLOG_REAL_TIME_BY_TOPIC_PREFIX = "sichao:blog:realTimeByTopic:";//+话题id     （话题下实时博客）





}
