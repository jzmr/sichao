package com.sichao.userService.controller;

import com.sichao.common.constant.Constant;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import com.sichao.userService.entity.User;
import com.sichao.userService.entity.vo.RegisterVo;
import com.sichao.userService.entity.vo.UpdateInfoVo;
import com.sichao.userService.entity.vo.UpdatePasswordVo;
import com.sichao.userService.entity.vo.UserInfoVo;
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
        //threadLocal中有数据时说明已登录，则不进行注册操作，避免前端卡段导致以登录用户进入注册页面
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map!=null)return R.error().message("已登录");
        userService.register(registerVo);
        return R.ok();
    }
    //登录
    @Operation(summary = "登录")
    @PostMapping("/login")
    public R login(@RequestBody User user) {//user要放手机号和密码
        //threadLocal中有数据时说明已登录，则不进行登录操作，避免前端卡段导致以登录用户进入登录页面
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map!=null)return R.error().message("已登录");
        String token=userService.login(user);
        return R.ok().data("token",token).data(Constant.COOKIE_EXPIRE,Constant.REFRESH_TOKEN_EXPIRE);
    }
    //根据token信息获取用户信息（密码除外）
    @Operation(summary = "根据token信息获取用户信息")
    @GetMapping("/getUserInfoByToken")
    public R getUserInfoByToken(HttpServletRequest request){
        //threadLocal中无数据时说明未登录 TODO
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");
        UserInfoVo userInfo=userService.getUserInfoByToken(Long.parseLong(map.get("userId")));
        return R.ok().data("userInfo",userInfo);
    }
    //注销
    @Operation(summary = "注销")
    @GetMapping("/logout")
    public R logout(HttpServletRequest request){
        //threadLocal中无数据时说明未登录
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
    //都要考虑幂等性 TODO
    //修改密码
    @Operation(summary = "修改密码")
    @PostMapping("/updatePassword")
    public R updatePassword(@RequestBody UpdatePasswordVo updatePasswordVo){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");
        userService.updatePassword(map.get(Constant.TOKEN),map.get("userId"),updatePasswordVo);
        return R.ok();
    }

    //根据用户id查看用户信息（密码除外）
    @Operation(summary = "根据用户id查看用户信息（密码除外）")
    @GetMapping("/getUserInfoById/{id}")
    public R getUserInfoById(@PathVariable("id") long id){
        UserInfoVo userInfo = userService.getUserInfoById(id);
        return R.ok().data("userInfo",userInfo);
    }

    //修改头像 TODO

    //修改用户个人信息（头像、密码除外）
    @Operation(summary = "修改用户个人信息（头像、密码除外）")
    @PostMapping("/updateInfo")
    public R updateInfo(@RequestBody UpdateInfoVo updateInfoVo){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");
        userService.updateInfo(Long.parseLong(map.get("userId")),updateInfoVo);//修改信息
        return getUserInfoById(Long.parseLong(map.get("userId")));//查询修改后的信息并返回
    }
}
