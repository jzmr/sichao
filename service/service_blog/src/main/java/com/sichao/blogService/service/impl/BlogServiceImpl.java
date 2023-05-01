package com.sichao.blogService.service.impl;

import com.sichao.blogService.entity.Blog;
import com.sichao.blogService.mapper.BlogMapper;
import com.sichao.blogService.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 博客表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
