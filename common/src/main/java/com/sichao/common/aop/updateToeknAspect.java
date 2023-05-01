package com.sichao.common.aop;

import com.sichao.common.constant.Constant;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @Description: token续签切面
 * @author: sjc
 * @createTime: 2023年04月30日 19:51
 */
@Aspect//表明是一个切面类
@Component//将当前切面类注入到Spring容器内
public class updateToeknAspect {
    //切入点
    @Pointcut("execution(public * com.sichao.*.controller.*.*(..))")//任意模块的controller包下的任意方法
    public void LogAspect(){}
    //前置通知：在方法前执行
//    @Before("LogAspect()")
//    public void doBefore(JoinPoint joinPoint){
//        System.out.println("doBefore");
//    }
    //后置通知：在方法后执行
//    @After("LogAspect()")
//    public void doAfter(JoinPoint joinPoint){
//        System.out.println("doAfter");
//    }
    //最终通知：在方法执行后返回一个结果后执行
//    @AfterReturning("LogAspect()")
//    public void doAfterReturning(JoinPoint joinPoint){
//        System.out.println("doAfterReturning");
//    }
    //异常通知：在方法执行过程中抛出异常的时候执行
//    @AfterThrowing("LogAspect()")
//    public void deAfterThrowing(JoinPoint joinPoint){
//        System.out.println("deAfterThrowing");
//    }
    //环绕通知 如果线程变量中token，则将token添加到R中一起返回给前端
    @Around("LogAspect()")
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
