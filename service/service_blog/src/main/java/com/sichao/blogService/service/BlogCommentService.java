package com.sichao.blogService.service;

import com.sichao.blogService.entity.BlogComment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.blogService.entity.vo.CommentVo;

import java.util.List;

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
    //查询指定博客id下的评论
    List<CommentVo> getCommentByBlogId(String userId, String blogId);
    //删除评论、博客评论数-1
    void deleteComment(String userId, String commentId);

    //========================================================
    //删除评论(批量)
    void deleteCommentBatchByBlogId(String blogId);

}
