package com.sichao.blogService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.entity.BlogTopicRelation;
import com.sichao.blogService.mapper.BlogTopicRelationMapper;
import com.sichao.blogService.service.BlogTopicRelationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    //要保证消费代码的幂等性，避免重复消费
    //批量绑定某个博客与多个话题之间的关系（此方法用来被rabbitMQ的消费者调用）
    @Transactional
    @Override
    public void blogBindingTopicBatch(String blogId, List<String> topicIdList) {
        //数据的正确性在发送消息之前就已经验证过了，所以这里不用验证数据是否正确
        QueryWrapper<BlogTopicRelation> wrapper = new QueryWrapper<>();
        BlogTopicRelation blogTopicRelation=null;
        for (String topicId : topicIdList) {
            wrapper.eq("topic_id",topicId);
            wrapper.eq("blog_id",blogId);
            BlogTopicRelation one = baseMapper.selectOne(wrapper);
            if(one!=null)continue;//数据已存在，跳过

            blogTopicRelation=new BlogTopicRelation();
            blogTopicRelation.setBlogId(blogId);
            blogTopicRelation.setTopicId(topicId);
            baseMapper.insert(blogTopicRelation);
        }
    }
}
