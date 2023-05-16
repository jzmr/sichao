package com.sichao.blogService.service;

import com.sichao.blogService.entity.BlogLikeUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户点赞博客关系表 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
public interface BlogLikeUserService extends IService<BlogLikeUser> {
    //点赞博客
    void likeBlog(String userId, String blogId);
    //取消点赞博客
    void unlikeBlog(String userId, String blogId);

    //=======================================
    //删除点赞关系(批量)并返回删除已点赞关系数目
    int deleteLikeBatchByBlogId(String blogId);
    //查看指定用户是否点赞指定博客
    boolean getIsLikeBlogByUserId(String userId, String blogId);
}
