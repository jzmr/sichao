package com.sichao.blogService.service;

import com.sichao.blogService.entity.BlogTopic;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.blogService.entity.vo.PublishTopicVo;
import com.sichao.blogService.entity.vo.TopicInfoVo;
import com.sichao.blogService.entity.vo.TopicTitleVo;

import java.util.List;

/**
 * <p>
 * 话题表 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
public interface BlogTopicService extends IService<BlogTopic> {
    //发布话题
    void publishTopic(PublishTopicVo publishTopicVo);
    //查询热门话题（热搜榜）
    List<TopicTitleVo> getHotTopicList();
    //获取某个话题的信息
    TopicInfoVo getTopicInfo(String topicId);
    //禁用话题
    void forbiddenTopicById(String id,String topicTitle);
    //启用话题
    void enableTopicById(String id, String topicTitle);
}
