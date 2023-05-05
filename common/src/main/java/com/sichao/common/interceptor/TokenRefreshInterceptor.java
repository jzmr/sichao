package com.sichao.common.interceptor;

import com.alibaba.fastjson2.JSON;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.entity.to.userInfoTo;
import com.sichao.common.utils.JwtUtils;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Description: token续签拦截器     实现toekn的续签与注销功能
 * @author: sjc
 * @createTime: 2023年04月30日 15:26
 */
@Component
@Slf4j
public class TokenRefreshInterceptor implements HandlerInterceptor {
    //threadLocal线程变量，这里用来在一个线程中保存数据的，便于同一线程内数据共享
    //threadLocal：用户每次请求结束后清除，不然可能当前这个tomcat的线程a保存有
    // 用户数据，然后别的用户也刚好用到了这个tomcat中的线程a，就发生了歧义
    public static ThreadLocal<HashMap<String,String>> threadLocal = new ThreadLocal<>();
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 预处理方法：该方法在处理器方法执行之前执行。其返回值为boolean，若为true，则紧接着会执行处理器方
     * 法，且会将afterCompletion()方法放入到一个专门的方法栈中等待执行。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String jwtToken = request.getHeader(Constant.TOKEN);
        if (!StringUtils.hasText(jwtToken)) return true;//为空说明没有登录，则不需要做token续签操作，直接放行

        boolean checkToken = JwtUtils.checkToken(jwtToken);//判断token是否有效

        //使用黑名单机制，解决以注销用户的token无法失效的问题
        Boolean hasKey = stringRedisTemplate.hasKey(PrefixKeyConstant.USER_BLACK_TOKEN_PREFIX + jwtToken);
        //为true说明该token在黑名单之中，则不操作，即不向ThreadLocal中保存信息，在之后的请求处理中是以用户未登录的状态去执行的
        if(Boolean.TRUE.equals(hasKey))return true;

        //Token自动续签
        if (checkToken) {//token未过期
            //将token字符串、用户id、用户昵称都保存到threadLocal线程变量中
            HashMap<String, String> map = new HashMap<>();
            map.put(Constant.TOKEN,jwtToken);
            map.put("userId",JwtUtils.getUserIdByJwtToken(jwtToken));
            map.put("nickname",JwtUtils.getNicknameByJwtToken(jwtToken));
            threadLocal.set(map);
        }else {//checkToken为false时，说明token过期，要进行token续签操作
            String userInfoStr = stringRedisTemplate.opsForValue().get(PrefixKeyConstant.USER_TOKEN_PREFIX + jwtToken);
            if (userInfoStr != null) {//续签时间不过期
                //获取当前用户信息
                userInfoTo userInfoTo = JSON.parseObject(userInfoStr, userInfoTo.class);
                String userId = userInfoTo.getUserId();
                String nickname = userInfoTo.getNickname();
                //根据续签token获取信息用来构建新token,1天
                String newJwtToken = JwtUtils.getJwtToken(userId, nickname, Constant.ACCESS_TOKEN_EXPIRE);
                //将新token保存到threadLocal中，在请求执行完毕之后，通过aop类将threadLocal中的新token保存在R对象中返回
                //将新token字符串、用户id、用户昵称都保存到threadLocal线程变量中
                HashMap<String, String> map = new HashMap<>();
                map.put(Constant.TOKEN,newJwtToken);
                map.put(Constant.NEW_TOKEN,newJwtToken);
                map.put("userId",userId);
                map.put("nickname",nickname);
                threadLocal.set(map);
                //以前缀+新token为key，将用户信息（id+nickname）放入redis中，过期时间为5天
                stringRedisTemplate.opsForValue().set(
                        PrefixKeyConstant.USER_TOKEN_PREFIX + newJwtToken,
                        userInfoStr,
                        Constant.REFRESH_TOKEN_EXPIRE,
                        TimeUnit.MILLISECONDS);
                //删除旧token的key
                stringRedisTemplate.delete(PrefixKeyConstant.USER_TOKEN_PREFIX + jwtToken);
            }
            //如果续签时间也过期，则不操作，即不向ThreadLocal中保存信息，则在之后的请求处理中是以用户未登录的状态去执行的
        }

        return true;//最后放行
    }

    /**
     * 后处理方法：该方法在处理器方法执行之后执行。处理器方法若最终未被执行，则该方法不会执行。 由于该方法
     * 是在处理器方法执行完后执行，且该方法参数中包含ModelAndView，所以该方法可以修 改处理器方法的处理结果
     * 数据，且可以修改跳转方向。
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
    }

    /**
     * 最后执行的方法：当preHandle()方法返回true时，会将该方法放到专门的方法栈中，等到对请求进行响应的所有
     * 工作完成之后才执行该方法。即该方法是在中央调度器渲染(数据填充)了响应页面之后执行的，此时对ModelAndView
     * 再操作也对响应无济于事。从用来清除资源。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        //threadLocal：用户每次请求结束后清除，不然可能当前这个tomcat的线程a保存有
        //用户数据，然后别的用户也刚好用到了这个tomcat中的线程a，就发生了歧义

        //删除此线程变量的当前线程值。如果当前线程随后读取了这个线程变量，它的值将通过调用它的initialvalue
        // 方法重新初始化，除非它的值是由当前线程在此期间设置的。这可能导致当前线程多次调用initialValue方法。
        threadLocal.remove();
    }

}
