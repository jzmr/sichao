package com.sichao.blogService.service.impl;

import com.sichao.blogService.entity.BlogComment;
import com.sichao.blogService.mapper.BlogCommentMapper;
import com.sichao.blogService.service.BlogCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评论与子评论表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogCommentServiceImpl extends ServiceImpl<BlogCommentMapper, BlogComment> implements BlogCommentService {

}
