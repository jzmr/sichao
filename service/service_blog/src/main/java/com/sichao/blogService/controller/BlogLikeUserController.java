package com.sichao.blogService.controller;

import com.sichao.blogService.entity.vo.PublishCommentVo;
import com.sichao.blogService.service.BlogLikeUserService;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * <p>
 * 用户点赞博客关系表 前端控制器
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@RestController
@RequestMapping("/blogService/blogLikeUser")
@Tag(name = "用户点赞博客模块")
public class BlogLikeUserController {
    @Autowired
    private BlogLikeUserService blogLikeUserService;

    //点赞博客
    @Operation(summary = "点赞博客")
    @PostMapping("/likeBlog/{blogId}")
    public R likeBlog(@PathVariable("blogId") String blogId){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        blogLikeUserService.likeBlog(map.get("userId"),blogId);
        return R.ok();
    }

    //取消点赞博客
    @Operation(summary = "取消点赞博客")
    @PostMapping("/unlikeBlog/{blogId}")
    public R unlikeBlog(@PathVariable("blogId") String blogId){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        blogLikeUserService.unlikeBlog(map.get("userId"),blogId);
        return R.ok();
    }


}
