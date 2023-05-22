package com.sichao.blogService.aop;

import com.alibaba.fastjson2.JSON;
import com.sichao.blogService.client.UserClient;
import com.sichao.blogService.service.BlogService;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Set;

/**
 * @Description: 博客切面类：提前加载用户feed流收件箱切面
 * @author: sjc
 * @createTime: 2023年04月30日 19:51
 *
 * 该切面放在博客模块是为了能使用blogService等相关的对象中的方法，
 * 而在博客模块的切面只对请求博客模块的请求生效，访问其他模块的请求不会触发此切面
 *
 */
@Aspect//表明是一个切面类
@Component//将当前切面类注入到Spring容器内
@Order(1)//指定该切面在AOP链中的优先级
public class BlogAspect {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserClient userClient;
    @Autowired
    private BlogService blogService;

    //切入点
    @Pointcut("execution(public * com.sichao.*.controller.*.*(..))")//任意模块的controller包下的任意方法
    public void controllerMethods(){}
    //前置通知：在方法前执行
    @Before("controllerMethods()")
    public void doBefore(JoinPoint joinPoint){//更新用户在线状态，当时间戳+2小时小于当前时间就说明已离线
        //保存用户在线状态时，以当前时间的时间戳为分值，查询用户是否在线时，如果用户的时间戳+2小时小于当前时间，则说明表示离线
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//使用时间戳为分值来实现过期时间
        String userOnlineKey = PrefixKeyConstant.USER_ONLINE_KEY;//用户在线列表key

        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map!=null && map.get("userId")!=null) {//用户已登录、则更新用户最新在线时间
            String userId = map.get("userId");
            //查看用户在线时间
            Double score = zSet.score(userOnlineKey, userId);
            LocalDateTime localDateTime = null;
            if(score != null){//在线列表中有该用户的在线数据，则将该时间戳转换成时间
                localDateTime = LocalDateTime.ofEpochSecond((long) (score/1000),0,ZoneOffset.ofHours(0));
            }//如果在线列表中没有该用户的在线数据，则localDateTime为null

            /**feed流离线拉：用户发送请求时，在博客切面的前置通知中，判断用户是否先前状态是离线，
             *      是离线则再判断该用户的收件箱是否存在，
             *          不存在则直接拉取前200条数据到缓存中，
             *          如果收件箱存在，则跟据zSet中最后一个元素的分值T，去拉起该用户的所有关注用户的发件箱中时间戳大于T的博客插入当前用户的收件箱中
             *      是在线状态时，关注的人发布博客会直接推送到收件箱，不需要在这里拉取
             */
            if(localDateTime==null || localDateTime.plusHours(2).compareTo(LocalDateTime.now())<0){//离线状态
                String followingBlogZSetKey = PrefixKeyConstant.BLOG_FOLLOWING_BLOG_PREFIX + userId;//feed流收件箱，关注用户的博客id的key
                if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(followingBlogZSetKey))){//收件箱存在
                    //获取最后一条博客的时间戳
                    long timestamp=0L;
                    Set<ZSetOperations.TypedTuple<String>> typedTuples = zSet.rangeWithScores(followingBlogZSetKey, -1, -1);//获取最后一个元素
                    if (typedTuples != null && !typedTuples.isEmpty()) {
                        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
                            Double score1 = typedTuple.getScore();
                            timestamp = (long) score1.doubleValue();
                        }
                    }
                    //获取关注列表
                    R r = userClient.getFollowingSetCache(userId);
                    String jsonString = JSON.toJSONString(r.getData().get("followingSet"));
                    Set<String> set = (Set<String>) JSON.parseObject(jsonString, Set.class);
                    //遍历每一个关注用户,
                    for (String followingUserId : set) {
                        String userBlogZSetKey = PrefixKeyConstant.BLOG_USER_BLOG_PREFIX + followingUserId;//feed流发件箱、用户的博客id的key
                        if(Boolean.FALSE.equals(stringRedisTemplate.hasKey(userBlogZSetKey))){//发件箱为空是，去获取发件箱
                            blogService.getUserBlog(userId,followingUserId,-2,0,0);
                        }
                        //获取分值大于timestamp的数据，并将数据插入当前用户的收件箱中
                        Set<ZSetOperations.TypedTuple<String>> tuples = zSet.rangeByScoreWithScores(userBlogZSetKey, timestamp, Double.POSITIVE_INFINITY);
                        if(tuples!=null && !tuples.isEmpty()){
                            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                                zSet.add(followingBlogZSetKey,tuple.getValue(), tuple.getScore());
                            }
                        }
                    }
                }else {//该用户的收件箱不存在，则去拉取前两百条数据
                    blogService.getFollowingBlog(userId,-2, 10,0);
                }
            }//在线状态时，关注的人发布博客会直接推送到收件箱
        }
    }
    //后置通知：在方法后执行
//    @After("controllerMethods()")
//    public void doAfter(JoinPoint joinPoint){
//        System.out.println("doAfter");
//    }
    //最终通知：在方法执行后返回一个结果后执行
//    @AfterReturning("controllerMethods()")
//    public void doAfterReturning(JoinPoint joinPoint){
//        System.out.println("doAfterReturning");
//    }
    //异常通知：在方法执行过程中抛出异常的时候执行
//    @AfterThrowing("controllerMethods()")
//    public void deAfterThrowing(JoinPoint joinPoint){
//        System.out.println("deAfterThrowing");
//    }
    //环绕通知 如果线程变量中token，则将token添加到R中一起返回给前端
//    @Around("controllerMethods()")
//    public Object deAround(ProceedingJoinPoint joinPoint) throws Throwable{
////        System.out.println("deAround");
//        //调用joinPoint的proceed()方法就会执行被切面方法，因为proceed()方法就是被切面的方
//        R r = (R) joinPoint.proceed();//执行方法，并将方法值从Obiect转换成R
//        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
//        if(map!=null && map.get(Constant.NEW_TOKEN)!=null) {//用户已登录 && 有新token需要续签
//            //向统一返回类R中添加新token、cookie过期时间，而后交由前端完成cookie的刷新
//            r.data(Constant.NEW_TOKEN, map.get(Constant.NEW_TOKEN)).data(Constant.COOKIE_EXPIRE,Constant.REFRESH_TOKEN_EXPIRE);
//        }
//        return r;//返回结果给前端
//    }

}
