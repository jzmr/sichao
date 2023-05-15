package com.sichao.blogService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.entity.BlogTopicRelation;
import com.sichao.blogService.mapper.BlogTopicRelationMapper;
import com.sichao.blogService.service.BlogTopicRelationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.common.constant.PrefixKeyConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    //要保证消费代码的幂等性，避免重复消费
    //批量绑定某个博客与多个话题之间的关系（此方法用来被rabbitMQ的消费者调用）
    @Transactional
    @Override
    public void blogBindingTopicBatch(String blogId, List<String> topicIdList) {
        //数据的正确性在发送消息之前就已经验证过了，所以这里不用验证数据是否正确
        QueryWrapper<BlogTopicRelation> wrapper = new QueryWrapper<>();
        BlogTopicRelation blogTopicRelation=null;
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        //TODO 循环查库了
        for (String topicId : topicIdList) {
            wrapper.eq("topic_id",topicId);
            wrapper.eq("blog_id",blogId);
            BlogTopicRelation one = baseMapper.selectOne(wrapper);
            if(one!=null)continue;//数据已存在，跳过

            blogTopicRelation=new BlogTopicRelation();
            blogTopicRelation.setBlogId(blogId);
            blogTopicRelation.setTopicId(topicId);
            baseMapper.insert(blogTopicRelation);

            //给话题在redis中增加讨论数
            String topicDiscussionModifyKey = PrefixKeyConstant.BLOG_TOPIC_DISCUSSION_MODIFY_PREFIX + topicId;//话题讨论数变化key
            //话题总讨论数+1
            ops.increment(topicDiscussionModifyKey);//自增，如果key不存在，则先创建整个key且值为0，而后再自增

        }
    }

    //根据博客id查询话题id
    @Override
    public List<String> getTopicIdByBlogId(String blogId) {
        QueryWrapper<BlogTopicRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("blog_id",blogId);
        wrapper.select("topic_id");
        List<BlogTopicRelation> list = baseMapper.selectList(wrapper);

        List<String> topicIdList = new ArrayList<>();
        for (BlogTopicRelation blogTopicRelation : list) {
            topicIdList.add(blogTopicRelation.getTopicId());
        }
        return topicIdList;
    }

    //删除博客与话题关系(批量)
    @Override
    public void deleteRelationBatchByBlogId(String blogId) {
        QueryWrapper<BlogTopicRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("blog_id",blogId);
        baseMapper.delete(wrapper);
    }
}
