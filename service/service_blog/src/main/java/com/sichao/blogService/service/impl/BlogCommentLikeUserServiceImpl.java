package com.sichao.blogService.service.impl;

import com.sichao.blogService.entity.BlogCommentLikeUser;
import com.sichao.blogService.mapper.BlogCommentLikeUserMapper;
import com.sichao.blogService.service.BlogCommentLikeUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户点赞评论关系表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogCommentLikeUserServiceImpl extends ServiceImpl<BlogCommentLikeUserMapper, BlogCommentLikeUser> implements BlogCommentLikeUserService {

}
