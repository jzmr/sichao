package com.sichao.blogService.service;

import com.sichao.blogService.entity.BlogComment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.blogService.entity.vo.CommentVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评论与子评论表 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
public interface BlogCommentService extends IService<BlogComment> {
    //发布评论(博客下评论、父评论)
    void saveComment(String curUserId, String blogId, String content);
    //删除评论、博客评论数-1
    void deleteComment(String userId, String commentId);
    //查询指定博客id下的评论（升序）
    Map<String,Object> getCommentByBlogId(String blogId, int start, int limit);
    //查询指定博客id下的评论（倒序）
    Map<String,Object> getCommentByBlogIdDesc(String blogId,int start,int limit);


    //========================================================
    //删除评论(批量)
    void deleteCommentBatchByBlogId(String blogId);

}
