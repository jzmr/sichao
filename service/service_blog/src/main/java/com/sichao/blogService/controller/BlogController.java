package com.sichao.blogService.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sichao.blogService.entity.Blog;
import com.sichao.blogService.entity.vo.BlogVo;
import com.sichao.blogService.entity.vo.PublishBlogVo;
import com.sichao.blogService.entity.vo.PublishTopicVo;
import com.sichao.blogService.service.BlogService;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

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
    @Operation(summary = "发布博客")
    @PostMapping("/saveBlog")
    public R saveBlog(@RequestBody PublishBlogVo publishBlogVo){
        //threadLocal中无数据时说明未登录 TODO
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        publishBlogVo.setCreatorId(map.get("userId"));
        blogService.saveBlog(publishBlogVo);
        return R.ok();
    }

    //分页查询指定话题id下的博客（根据博客评论数+点赞数倒序）（并查询当前用户使用点赞该博客，未登录则默认未点赞）
    @Operation(summary = "分页查询指定话题id下的博客")
    @GetMapping("/getBlogByTopicId/{topicId}/{page}/{limit}")
    public R getBlogByTopicId(@PathVariable("topicId") String topicId,@PathVariable("page") int page,@PathVariable("limit") int limit){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        String userId=null;
        if(map!=null)userId=map.get("userId");

        PageHelper.startPage(page, limit);
        List<BlogVo> blogList = blogService.getBlogByTopicId(userId,topicId);
        PageInfo pageInfo=new PageInfo(blogList);
        return R.ok().data("blogList",blogList).data("pageInfo",pageInfo);
    }

}
