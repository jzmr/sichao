package com.sichao.service_oss.controller;

import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.JwtUtils;
import com.sichao.common.utils.R;
import com.sichao.service_oss.client.UserClient;
import com.sichao.service_oss.service.OssService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Description: OSS对象存储控制类
 * @author: sjc
 * @createTime: 2023年05月07日 14:32
 */
@RestController
@RequestMapping("/ossService/oss")
@CrossOrigin//跨域解决方法
@Tag(name = "OSS对象存储模块")//将该Controller类下的接口放入knife4j中，并命名为“OSS对象存储模块”
public class OssController {
    @Autowired
    private OssService ossService;
    @Autowired
    private UserClient userClient;

    //上传文件的方法(☆)
    @PostMapping(value = "/uploadFile/{userId}/{imgNo}")//MultipartFile这个类型的参数可以获得前端上传的文件
    public R uploadFile(@PathVariable("userId") String userId, @PathVariable("imgNo") String imgNo, MultipartFile file){
        System.out.println(file.getOriginalFilename());
        //上传文件并返回文件在oss中的url地址
        String url=ossService.uploadFile(file);

        //将url保存到redis中,设置有效时长
//        redisTemplate.opsForValue().set(userId+"_"+imgNo,url,5, TimeUnit.MINUTES);//设置5分钟的有效时间
        return R.ok();
    }

    //上传头像图片文件的方法(☆)（细化方法）
    //这里上传文件时的请求是没有经过前端的请求拦截器的，所以不会在请求头中带上token，所以以参数的形式传入token
    @PostMapping(value = "/uploadAvatar")//MultipartFile这个类型的参数可以获得前端上传的文件
    public R uploadAvatar(String token,MultipartFile file){
        //根据token获取id
        String userId = JwtUtils.getUserIdByJwtToken(token);
        //上传文件并返回文件在oss中的url地址
        String url=ossService.uploadFile(file);
        //远程调用修改头像url的方法
        return userClient.updateAvatarUrl(userId,url);
    }


}
