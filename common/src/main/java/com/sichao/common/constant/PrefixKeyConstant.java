package com.sichao.common.constant;

/**
 * @Description: 前缀key常量类,常用redis的key前缀
 * @author: sjc
 * @createTime: 2023年04月29日 21:53
 */
public class PrefixKeyConstant {//项目名:业务名:类型:id

    public static final String SMS_CODE_PREFIX = "sichao:sms:code:";//+手机号

    public static final String USER_TOKEN_PREFIX = "sichao:user:token:";//+token值
    public static final String USER_BLACK_TOKEN_PREFIX = "sichao:user:blackToken:";//+token值    （黑名单Token前缀）


}
