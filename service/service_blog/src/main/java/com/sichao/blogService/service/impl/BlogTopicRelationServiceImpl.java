package com.sichao.blogService.service.impl;

import com.sichao.blogService.entity.BlogTopicRelation;
import com.sichao.blogService.mapper.BlogTopicRelationMapper;
import com.sichao.blogService.service.BlogTopicRelationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 话题与博客关系表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogTopicRelationServiceImpl extends ServiceImpl<BlogTopicRelationMapper, BlogTopicRelation> implements BlogTopicRelationService {

}
