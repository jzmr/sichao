package com.sichao.blogService.service.impl;

import com.sichao.blogService.entity.BlogLikeUser;
import com.sichao.blogService.mapper.BlogLikeUserMapper;
import com.sichao.blogService.service.BlogLikeUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户点赞博客关系表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogLikeUserServiceImpl extends ServiceImpl<BlogLikeUserMapper, BlogLikeUser> implements BlogLikeUserService {

}
