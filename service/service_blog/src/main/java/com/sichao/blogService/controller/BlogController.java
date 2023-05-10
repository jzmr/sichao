package com.sichao.blogService.controller;

import com.sichao.blogService.entity.Blog;
import com.sichao.blogService.entity.vo.PublishBlogVo;
import com.sichao.blogService.entity.vo.PublishTopicVo;
import com.sichao.blogService.service.BlogService;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * <p>
 * 博客表 前端控制器
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@RestController
@RequestMapping("/blogService/blog")
@Tag(name = "博客模块")
public class BlogController {
    @Autowired
    private BlogService blogService;


    //发布博客
    @PostMapping("/saveBlog")
    public R saveBlog(@RequestBody PublishBlogVo publishBlogVo){
        //threadLocal中无数据时说明未登录 TODO
//        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
//        if(map==null)return R.error().message("未登录");

//        publishBlogVo.setCreatorId(map.get("userId"));
        blogService.saveBlog(publishBlogVo);
        return R.ok();
    }

}
