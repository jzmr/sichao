package com.sichao.common.utils;

/**
 * @Description: 随机过期时间类
 * @author: sjc
 * @createTime: 2023年05月04日 19:45
 */
public class RandomSxpire {
    //返回0~10分钟之间的随机毫秒值，用来防止缓存雪崩
    public static long getRandomSxpire(){
        return (long) (Math.random()*1000*60*10);
    }

}
