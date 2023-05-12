package com.sichao.blogService.service;

import com.sichao.blogService.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.blogService.entity.vo.BlogVo;
import com.sichao.blogService.entity.vo.PublishBlogVo;

import java.util.List;

/**
 * <p>
 * 博客表 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
public interface BlogService extends IService<Blog> {
    //发布博客
    void saveBlog(PublishBlogVo publishBlogVo);
    //分页查询指定话题id下的博客
    List<BlogVo> getBlogByTopicId(String userId, String topicId);
}
