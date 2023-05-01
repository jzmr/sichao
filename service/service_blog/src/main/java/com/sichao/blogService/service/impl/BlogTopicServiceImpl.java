package com.sichao.blogService.service.impl;

import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.mapper.BlogTopicMapper;
import com.sichao.blogService.service.IBlogTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 话题表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogTopicServiceImpl extends ServiceImpl<BlogTopicMapper, BlogTopic> implements IBlogTopicService {

}
