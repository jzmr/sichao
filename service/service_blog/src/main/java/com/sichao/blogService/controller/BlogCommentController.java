package com.sichao.blogService.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sichao.blogService.entity.vo.BlogVo;
import com.sichao.blogService.entity.vo.CommentVo;
import com.sichao.blogService.entity.vo.PublishBlogVo;
import com.sichao.blogService.entity.vo.PublishCommentVo;
import com.sichao.blogService.service.BlogCommentService;
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
 * 评论与子评论表 前端控制器
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@RestController
@RequestMapping("/blogService/blogComment")
@Tag(name = "博客评论模块")
public class BlogCommentController {
    @Autowired
    private BlogCommentService blogCommentService;
    //发布评论
    @Operation(summary = "发布评论")
    @PostMapping("/saveComment")
    public R saveComment(@RequestBody PublishCommentVo publishCommentVo){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        blogCommentService.saveComment(map.get("userId"),publishCommentVo.getBlogId(), publishCommentVo.getContent());
        return R.ok();
    }

    //删除评论、博客评论数-1
    @Operation(summary = "删除评论、博客评论数-1")
    @DeleteMapping("/deleteComment/{commentId}")
    public R deleteComment(@PathVariable("commentId") String commentId){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        blogCommentService.deleteComment(map.get("userId"),commentId);
        return R.ok();
    }

    //分页查询指定博客id下的评论 (使用redis缓存)（根据发布时间升序）（并查询当前用户使用点赞该博客，未登录则默认未点赞）
    @Operation(summary = "分页查询指定博客id下的评论（根据发布时间升序）")
    @GetMapping("/getCommentByBlogId/{blogId}/{start}/{limit}")
    public R getCommentByBlogId(@PathVariable("blogId") String blogId,@PathVariable("start") int start,@PathVariable("limit") int limit){
        Map<String, Object> commentMap = blogCommentService.getCommentByBlogId(blogId, start, limit);

        if(commentMap == null)return R.ok().data("commentList",null);
        return R.ok().data("commentList",commentMap.get("commentList")).data("end",commentMap.get("end"));
    }

    //分页查询指定博客id下的评论(使用redis缓存)（根据发布时间倒序）（并查询当前用户使用点赞该博客，未登录则默认未点赞）
    @Operation(summary = "分页查询指定博客id下的评论（根据发布时间倒序）")
    @GetMapping("/getCommentByBlogIdDesc/{blogId}/{start}/{limit}")
    public R getCommentByBlogIdDesc(@PathVariable("blogId") String blogId,@PathVariable("start") int start,@PathVariable("limit") int limit){
        Map<String,Object> commentMap = blogCommentService.getCommentByBlogIdDesc(blogId,start,limit);

        if(commentMap == null)return R.ok().data("commentList",null);
        return R.ok().data("commentList",commentMap.get("commentList")).data("end",commentMap.get("end"));
    }


}
