package com.sichao.blogService.client;

import com.sichao.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}