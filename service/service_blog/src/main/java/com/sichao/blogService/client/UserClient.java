package com.sichao.blogService.client;

import com.sichao.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * @Description: Feign远程接口
 * @author: sjc
 * @createTime: 2023年05月07日 16:03
 */
//1、指定从哪个服务中调用功能，名称与被调用的服务名保持一致
//2、指定触发熔断机制去执行那个类里的方法，（那个方法触发熔断，默认就去指定类找同名的兜底方法）
@FeignClient(value = "service-user")
@Component//交给spring管理
public interface UserClient {
    //定义调用的方法路径//写完全路径，因为不在同一个服务内
    //@PathVariable注解一定要指定参数名称，否则出错
    //多个参数时要有RequestParam做区分，否注出错

    //根据昵称（用户名）查询用户id(用于远程调用)
    @Operation(summary = "根据昵称（用户名）查询用户id")
    @GetMapping("/userService/user/getUserIdByNickname")
    public R getUserIdByNickname(@RequestParam("nickname") String nickname);

    //根据用户id查询用户信息(用于远程调用)
    @Operation(summary = "根据用户id查询用户信息(用于远程调用)")
    @GetMapping("/userService/user/getUserById")
    public R getUserById(@RequestParam("id") String id);

    //根据用户id对其博客数+1(用于远程调用)
    @Operation(summary = "根据用户id对其博客数+1(用于远程调用)")
    @PostMapping("/userService/user/userBlogCountPlusOne")
    public R userBlogCountPlusOne(@RequestParam("id") String id);

    //根据用户id对其博客数-1(用于远程调用)
    @Operation(summary = "根据用户id对其博客数-1(用于远程调用)")
    @PostMapping("/userService/user/userBlogCountMinusOne")
    public R userBlogCountMinusOne(@RequestParam("id") String id);

    //获取用户关注列表(用于远程调用)
    @Operation(summary = "获取用户关注列表")
    @GetMapping("/userService/userFollow/getFollowingSetCache")
    public R getFollowingSetCache(@RequestParam("userId") String userId);

    //获取用户粉丝列表(用于远程调用)
    @Operation(summary = "获取用户粉丝列表")
    @GetMapping("/userService/userFollow/getFollowerSetCache")
    public R getFollowerSetCache(@RequestParam("userId") String userId);
}
