package com.sichao.common.constant;

/**
 * @Description: 公共模块-常量类
 * @author: sjc
 * @createTime: 2023年04月26日 22:35
 */

public class Constant {
//    public static final int SUCCESS_CODE = 200; //请求成功
//    public static final int FAILURE_CODE = 500; //服务器内部错误
    public static final int SUCCESS_CODE = 20000; //请求成功
    public static final int FAILURE_CODE = 20001; //服务器内部错误
    public static final int TOKEN_EXPIRE_CODE = 20002; //登录过期

    public static final int PARAMS_ERROR_CODE = 400; //请求参数错误
    public static final int UNAUTHORIZED_CODE = 401; //未授权
    public static final int FORBIDDEN_CODE = 403; //禁止访问

    public static final String SUCCESS_MESSAGE = "请求成功";
    public static final String FAILURE_MESSAGE = "服务器内部错误";
    public static final String TOKEN_EXPIRE_MESSAGE = "登录过期";
    public static final String PARAMS_ERROR_MESSAGE = "请求参数错误";
    public static final String UNAUTHORIZED_MESSAGE = "未授权";
    public static final String FORBIDDEN_MESSAGE = "禁止访问";

    public static final String TOKEN = "token";
    public static final String NEW_TOKEN = "newToken";
    public static final String COOKIE_EXPIRE = "cookieExpire";

    public static final long ACCESS_TOKEN_EXPIRE = 1000 * 60 * 60 * 24;//单为毫秒，1天
    public static final long REFRESH_TOKEN_EXPIRE = 1000 * 60 * 60 * 24 * 5;//单位毫秒，5天

    //TODO 应该根据配置文件数据注入到这里
    public static final String TOKEN_SECRET_KEY = "ukc8BDbRigUDaY6pZFfWus2jZWLPHO";//token秘钥（每个公司都会各自生成不同的秘钥）
    public static final long TOKEN_SECRET_KEY_DAY_PREFIX = 1000 * 60 * 60 * 24 * 15;//token秘钥前缀过期时间 TODO


    public static final long ONE_HOURS_EXPIRE = 1000*60*60;//单位毫秒，1小时
    public static final long FIVE_MINUTES_EXPIRE = 1000*60*5;//单位毫秒，5分钟
    public static final long THREE_DAYS_EXPIRE = 1000L*60*60*24*3;//单位毫秒，3天
    public static final long THIRTY_DAYS_EXPIRE = 1000L*60*60*24*30;//单位毫秒，30天

    //@用户 超链接拼接常量
    //<a href="/user/1231243512351431234" style="color:orange;">@aaa</a>
    public static final String BLOG_AT_USER_HYPERLINK_PREFIX = "<a href=\"/user/";//+用户id（@用户超链接前缀）
    public static final String BLOG_AT_USER_HYPERLINK_INFIX = "\" style=\"color:orange;\">";//+@用户名（@用户超链接中缀）
    public static final String BLOG_AT_USER_HYPERLINK_SUFFIX = "</a>";//（@用户超链接后缀）


    //#话题# 超链接拼接常量/blog/topic
    //<a href="/blog/topic/1231243512351431234" style="color:blue;">#bbb#</a>
    public static final String BLOG_AT_TOPIC_HYPERLINK_PREFIX = "<a href=\"/blog/topic/";//+话题id    （#话题#超链接前缀）
    public static final String BLOG_AT_TOPIC_HYPERLINK_INFIX = "\" style=\"color:blue;\">";//+#话题title# （#话题#超链接中缀）
    public static final String BLOG_AT_TOPIC_HYPERLINK_SUFFIX = "</a>";//（#话题#超链接后缀）

    //博客详情超链接拼接常量
    //<a href="/blog/1231243512351431234" style="color:red;">(bbb)</a>
    public static final String BLOG_DETAIL_HYPERLINK_PREFIX = "<a href=\"/blog/";//+博客id    （博客详情超链接前缀）
    public static final String BLOG_DETAIL_HYPERLINK_INFIX = "\" style=\"color:red;\">";//+博客内容 （博客详情超链接中缀）
    public static final String BLOG_DETAIL_HYPERLINK_SUFFIX = "</a>";//（博客详情超链接后缀）

    public static final String BLOG_AT_USER_OFFICIAL_USER_ID = "1662351181822836737";//（官方的博客@助手的用户id）
    public static final String BLOG_DELETE_PREFIX = "Delete";//（博客或评论删除时修改zset类型中的value值时添加的前缀）
    public static final int ONLINE_TIME = 2;//单位：小时 （判断用户是否在线的时间间隔）
}
