package com.sichao.blogService.service.impl;

import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.entity.vo.PublishTopicVo;
import com.sichao.blogService.mapper.BlogTopicMapper;
import com.sichao.blogService.service.BlogTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
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
public class BlogTopicServiceImpl extends ServiceImpl<BlogTopicMapper, BlogTopic> implements BlogTopicService {

    //发布话题
    @Override
    public void publishTopic(PublishTopicVo publishTopicVo) {
        BlogTopic blogTopic = new BlogTopic();
        BeanUtils.copyProperties(publishTopicVo, blogTopic);
        //设置默认话题图标
        blogTopic.setIconUrl("http://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83eoj0hHXhgJNOTSOFsS4uZs8x1ConecaVOB8eIl115xmJZcT4oCicvia7wMEufibKtTLqiaJeanU2Lpg3w/132");
        baseMapper.insert(blogTopic);
    }
}
