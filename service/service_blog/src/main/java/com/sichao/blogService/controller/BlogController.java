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
import java.util.Map;

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
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        publishBlogVo.setCreatorId(map.get("userId"));
        blogService.saveBlog(publishBlogVo);
        return R.ok();
    }

    //删除博客及其下的所有评论，以及点赞关系、话题关系、并自减各个数据
    @Operation(summary = "删除博客及其下的所有评论，以及点赞关系、话题关系、并自减各个数据")
    @DeleteMapping("/deleteBlog/{blogId}")
    public R deleteBlog(@PathVariable("blogId") String blogId){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        blogService.deleteBlog(map.get("userId"),blogId);
        return R.ok();
    }


    //分页查询指定话题id下的博客（使用redis缓存）（根据博客评论数+点赞数倒序）（并查询当前用户使用点赞该博客，未登录则默认未点赞）
    @Operation(summary = "分页查询指定话题id下的博客")
    @GetMapping("/getBlogByTopicId/{topicId}/{page}/{limit}")
    public R getBlogByTopicId(@PathVariable("topicId") String topicId,@PathVariable("page") int page,@PathVariable("limit") int limit){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        String userId=null;
        if(map!=null)userId=map.get("userId");

        List<BlogVo> blogList = blogService.getBlogByTopicId(userId,topicId,page,limit);
        return R.ok().data("blogList",blogList);
    }


    //分页查询指定话题id下的实时博客（使用redis缓存）（根据博客发布时间倒序）（并查询当前用户使用点赞该博客，未登录则默认未点赞）
    @Operation(summary = "分页查询指定话题id下的实时博客")
    @GetMapping("/getRealTimeBlogByTopicId/{topicId}/{start}/{limit}")
    public R getRealTimeBlogByTopicId(@PathVariable("topicId") String topicId,@PathVariable("start") int start,@PathVariable("limit") int limit){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        String userId=null;
        if(map!=null)userId=map.get("userId");

        Map<String,Object> blogMap = blogService.getRealTimeBlogByTopicId(userId,topicId,start,limit);

        if(blogMap == null)return R.ok().data("blogList",null);
        return R.ok().data("blogList",blogMap.get("blogVoList")).data("end",blogMap.get("end"));
    }

    //查询用户博客（使用redis缓存）（根据博客发布时间倒序）（并查询当前用户使用点赞该博客，未登录则默认未点赞）
    @Operation(summary = "查询用户博客")
    @GetMapping("/getUserBlog/{targetUserId}/{start}/{limit}/{startTimestamp}")
    public R getUserBlog(@PathVariable("targetUserId")String targetUserId,@PathVariable("start")int start,@PathVariable("limit")int limit,@PathVariable("startTimestamp") String startTimestamp){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        String userId=null;
        if(map!=null)userId=map.get("userId");

        Map<String,Object> blogMap = blogService.getUserBlog(userId,targetUserId,start,limit,Long.parseLong(startTimestamp));

        if(blogMap == null)return R.ok().data("blogList",null);
        return R.ok().data("blogList",blogMap.get("blogVoList")).data("end",blogMap.get("end")).data("startTimestamp",blogMap.get("startTimestamp"));
    }


    //查询我的关注用户的博客（使用redis缓存）（根据博客发布时间倒序）（并查询当前用户使用点赞该博客，未登录则默认未点赞）
    @Operation(summary = "分页查询我的关注用户的博客")
    @GetMapping("/getMyFeedBlog/{start}/{limit}")
    public R getMyFeedBlog(@PathVariable("start")int start,@PathVariable("limit")int limit){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        List<BlogVo> blogList = blogService.getMyFeedBlog(map.get("userId"),start,limit);

//        if(blogMap == null)return R.ok().data("blogList",null);
        return R.ok().data("blogList",blogList);
    }
}
