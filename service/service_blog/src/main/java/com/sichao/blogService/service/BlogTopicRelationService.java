package com.sichao.blogService.service;

import com.sichao.blogService.entity.BlogTopicRelation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 话题与博客关系表 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
public interface BlogTopicRelationService extends IService<BlogTopicRelation> {

    //批量绑定某个博客与多个话题之间的关系（此方法用来被rabbitMQ的消费者调用）
    void blogBindingTopicBatch(String blogId,List<String> topicIdList);

    //根据博客id查询话题id
    List<String> getTopicIdByBlogId(String blogId);

    //删除博客与话题关系(批量)
    void deleteRelationBatchByBlogId(String blogId);
    //查询话题下所有博客id
    List<BlogTopicRelation> getBlogListByTopicId(String topicId);
    //查询话题下所有实时博客id
    List<BlogTopicRelation> getRealTimetBlogListByTopicId(String topicId);
}
