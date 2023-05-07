package com.sichao.service_oss.client;

import com.sichao.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description: Feign远程接口
 * @author: sjc
 * @createTime: 2023年05月07日 16:03
 */
//1、指定从哪个服务中调用功能，名称与被调用的服务名保持一致
//2、指定触发熔断机制去执行那个类里的方法，（那个方法触发熔断，默认就去指定类找同名的兜底方法）
@FeignClient(value = "service-user",fallback = UserDegradeFeignClient.class)
@Component//交给spring管理
public interface UserClient {
    //定义调用的方法路径//写完全路径，因为不在同一个服务内
    //@PathVariable注解一定要指定参数名称，否则出错
    //多个参数时要有RequestParam做区分，否注出错


    //修改头像url,用来被OSS模块远程调用，其传过来一个图片上传返回的地址，这个方法将该地址持久化到用户表中
    @Operation(summary = "修改头像Url")
    @PostMapping("/userService/user/updateAvatarUrl")
    public R updateAvatarUrl(@RequestParam("userId") String userId,@RequestParam("avatarUrl")String avatarUrl);
}
