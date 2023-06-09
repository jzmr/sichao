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
    public static final String USER_ONLINE_KEY = "sichao:user:online";//    （全局维护的在线用户列表）
    public static final String USER_CRON_TASK_LOCK_PREFIX = "sichao:user:cronTaskLcok:";//+具体定时任务名  （用户模块定时任务锁）
    public static final String USER_INFO_PREFIX = "sichao:user:info:";//+用户id   （用户信息）
    public static final String USER_INFO_LOCK_PREFIX = "sichao:user:infoLock:";//+用户id   （用户信息锁）
    public static final String USER_FOLLOWER_MODIFY_PREFIX = "sichao:user:followerModify:";//+用户id  （用户粉丝数变化）
    public static final String USER_FOLLOWING_MODIFY_PREFIX = "sichao:user:followingModify:";//+用户id  （用户关注数变化）
    public static final String USER_LIKE_COUNT_MODIFY_PREFIX = "sichao:user:likeCountModify:";//+用户id  （用户总获得点赞数变化数）
    public static final String USER_FOLLOWING_LIST_PREFIX = "sichao:user:followingList:";//+用户id  （用户关注列表）
    public static final String USER_FOLLOWING_LIST_LOCK_PREFIX = "sichao:user:followingListLock:";//+用户id   （用户关注列表锁）
    public static final String USER_FOLLOWER_LIST_PREFIX = "sichao:user:followerList:";//+用户id  （用户粉丝列表）
    public static final String USER_FOLLOWER_LIST_LOCK_PREFIX = "sichao:user:followerListLock:";//+用户id   （用户粉丝列表锁）



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
    public static final String BLOG_COMMENT_VO_INFO_PREFIX = "sichao:blog:commentVoInfo:";//+评论id     （评论vo信息）
    public static final String BLOG_COMMENT_VO_INFO_LOCK_PREFIX = "sichao:blog:commentVoInfoLock:";//+评论id   （评论vo信息锁）

    public static final String BLOG_BY_TOPIC_LOCK_PREFIX = "sichao:blog:byTopicLock:";//+话题id   （话题下综合博客查询锁）
    public static final String BLOG_BY_TOPIC_PREFIX = "sichao:blog:byTopic:";//+话题id     （话题下综合博客）
    public static final String BLOG_REAL_TIME_BY_TOPIC_LOCK_PREFIX = "sichao:blog:realTimeByTopicLock:";//+话题id   （话题下实时博客的id查询锁）
    public static final String BLOG_REAL_TIME_BY_TOPIC_PREFIX = "sichao:blog:realTimeByTopic:";//+话题id     （话题下实时博客id）
    public static final String BLOG_COMMENT_LOCK_PREFIX = "sichao:blog:commentLock:";//+博客id   （博客下评论id查询锁）
    public static final String BLOG_COMMENT_PREFIX = "sichao:blog:comment:";//+博客id     （博客下评论id）
    public static final String BLOG_USER_BLOG_LOCK_PREFIX = "sichao:blog:userBlogLock:";//+用户id   （feed流发件箱：用户的博客的id查询锁）
    public static final String BLOG_USER_BLOG_PREFIX = "sichao:blog:userBlog:";//+用户id     （feed流发件箱：用户的博客id）
    public static final String BLOG_FOLLOWING_BLOG_LOCK_PREFIX = "sichao:blog:followingBlogLock:";//+用户id   （feed流收件箱锁：关注用户的博客的id查询锁）
    public static final String BLOG_FOLLOWING_BLOG_PREFIX = "sichao:blog:followingBlog:";//+用户id     （feed流收件箱：关注用户的博客id）



    //消息模块
    public static final String MESSAGE_WEBSOCKET_PREFIX = "sichao:message:webScoketSession:";//+用户Id   （指定用户id的webSocket会话）

}
