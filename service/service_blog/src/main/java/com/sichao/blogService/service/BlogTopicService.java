package com.sichao.blogService.service;

import com.sichao.blogService.entity.BlogTopic;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.blogService.entity.vo.PublishTopicVo;

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
}
