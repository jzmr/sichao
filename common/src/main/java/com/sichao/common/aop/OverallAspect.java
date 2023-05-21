package com.sichao.common.aop;

import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import jakarta.annotation.Resource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

/**
 * @Description: 全局切面类：token续签切面、更新用户在线状态切面
 * @author: sjc
 * @createTime: 2023年04月30日 19:51
 */
@Aspect//表明是一个切面类
@Component//将当前切面类注入到Spring容器内
public class OverallAspect {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //切入点
    @Pointcut("execution(public * com.sichao.*.controller.*.*(..))")//任意模块的controller包下的任意方法
    public void controllerMethods(){}
    //前置通知：在方法前执行
    @Before("controllerMethods()")
    public void doBefore(JoinPoint joinPoint){//更新用户在线状态，当时间戳+2小时小于当前时间就说明已离线
        //保存用户在线状态时，以当前时间的时间戳为分值，查询用户是否在线时，如果用户的时间戳+2小时小于当前时间，则说明表示离线
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();//使用时间戳为分值来实现过期时间
        String userOnlineKey = PrefixKeyConstant.USER_ONLINE_KEY;

        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map!=null && map.get("userId")!=null) {//用户已登录、则更新用户最新在线时间
            LocalDateTime dateTime = LocalDateTime.now();//获取当前时间
            long timestamp = dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();//将LocalDateTime转换成Unix时间戳
            zSet.add(userOnlineKey,map.get("userId"),timestamp);//(key,用户id，时间戳)
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
    @Around("controllerMethods()")
    public Object deAround(ProceedingJoinPoint joinPoint) throws Throwable{
//        System.out.println("deAround");
        //调用joinPoint的proceed()方法就会执行被切面方法，因为proceed()方法就是被切面的方
        R r = (R) joinPoint.proceed();//执行方法，并将方法值从Obiect转换成R
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map!=null && map.get(Constant.NEW_TOKEN)!=null) {//用户已登录 && 有新token需要续签
            //向统一返回类R中添加新token、cookie过期时间，而后交由前端完成cookie的刷新
            r.data(Constant.NEW_TOKEN, map.get(Constant.NEW_TOKEN)).data(Constant.COOKIE_EXPIRE,Constant.REFRESH_TOKEN_EXPIRE);
        }
        return r;//返回结果给前端
    }

}
