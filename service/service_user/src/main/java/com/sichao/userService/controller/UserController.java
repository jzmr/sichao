package com.sichao.userService.controller;

import com.sichao.common.constant.Constant;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.JwtUtils;
import com.sichao.common.utils.R;
import com.sichao.userService.entity.User;
import com.sichao.userService.entity.vo.RegisterVo;
import com.sichao.userService.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author jicong
 * @since 2023-04-28
 */
@RestController
@RequestMapping("/userService/user")
@CrossOrigin//跨域解决方法
@Tag(name = "用户模块")//将该Controller类下的接口放入knife4j中，并命名为“用户模块”
public class UserController {
    @Autowired
    private IUserService userService;
    //注册
    @Operation(summary = "注册")//对该接口在knife4j中命名为“注册”
    @PostMapping("/register")
    public R register(@RequestBody RegisterVo registerVo){
        userService.register(registerVo);
        return R.ok();
    }
    //登录
    @Operation(summary = "登录")
    @PostMapping("/login")
    public R login(@RequestBody User user) {//user要放手机号和密码
        String token=userService.login(user);
        return R.ok().data("token",token).data(Constant.COOKIE_EXPIRE,Constant.REFRESH_TOKEN_EXPIRE);
    }
    //根据token信息获取用户信息
    @Operation(summary = "根据token信息获取用户信息")
    @GetMapping("/getUserInfo")
    public R getUserInfo(HttpServletRequest request){
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");
        User user = userService.getById(map.get("userId"));
        return R.ok().data("userInfo",user);
    }
    //注销
    @Operation(summary = "注销")
    @GetMapping("/logout")
    public R logout(HttpServletRequest request){
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");
        userService.logout(map.get(Constant.TOKEN));
        return R.ok();
    }

    //查看用户是否被禁用
    @Operation(summary = "查看用户是否被禁用")
    @GetMapping("/userIsDisabled/{userId}")
    public R userIsDisabled(@PathVariable String userId){
        boolean result=userService.userIsDisabled(userId);
        return R.ok().data("result",result);
    }

    // TODO 修改用户信息（乐观锁操作）
    // TODO 禁用与开放用户
    // TODO 系统向用户发送消息



}
