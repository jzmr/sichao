package com.sichao.blogService.service.impl;

import com.alibaba.fastjson2.JSON;
import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.entity.vo.PublishTopicVo;
import com.sichao.blogService.entity.vo.TopicInfoVo;
import com.sichao.blogService.entity.vo.TopicTitleVo;
import com.sichao.blogService.mapper.BlogTopicMapper;
import com.sichao.blogService.service.BlogTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.exceptionhandler.sichaoException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    //发布话题
    @Transactional
    @Override
    public void publishTopic(PublishTopicVo publishTopicVo) {
        BlogTopic blogTopic = new BlogTopic();
        BeanUtils.copyProperties(publishTopicVo, blogTopic);
        //设置默认话题图标
        if(blogTopic.getIconUrl()==null)
            blogTopic.setIconUrl("http://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83eoj0hHXhgJNOTSOFsS4uZs8x1ConecaVOB8eIl115xmJZcT4oCicvia7wMEufibKtTLqiaJeanU2Lpg3w/132");
        baseMapper.insert(blogTopic);
    }

    //查询热门话题（热搜榜）
    @Override
    public List<TopicTitleVo> getHotTopicList() {
        //热搜榜key
        String hotTopicKey = PrefixKeyConstant.BLOG_HOT_TOPIC_KEY;
        //根据话题热度倒序（从大到小）获取50条话题
        //保存在redis中的zSet类型中的数据，默认是升序，查询时在命令的Z后面添加REV可降序查询
        Set<String> set = stringRedisTemplate.opsForZSet().reverseRange(hotTopicKey, 0, 49);
        if(set==null)return null;
        List<TopicTitleVo> list = new ArrayList<>();
        for (String str : set) {
            TopicTitleVo topicTitleVo = JSON.parseObject(str, TopicTitleVo.class);
            list.add(topicTitleVo);
        }
        return list;
    }

    //获取某个话题的信息
    @Override
    public TopicInfoVo getTopicInfo(String topicId) {
        BlogTopic blogTopic = baseMapper.selectById(topicId);
        if(blogTopic==null || blogTopic.getStatus()==0)throw new sichaoException(Constant.FAILURE_CODE,"该话题已被禁用");
        TopicInfoVo topicInfo = new TopicInfoVo();
        BeanUtils.copyProperties(blogTopic, topicInfo);
        return topicInfo;
    }
    //禁用话题
    @Transactional
    @Override
    public void forbiddenTopicById(String id,String topicTitle) {
        //禁用话题
        BlogTopic blogTopic = new BlogTopic();
        blogTopic.setId(id);
        blogTopic.setStatus((byte) 0);
        baseMapper.updateById(blogTopic);

        //要删除热搜榜缓冲中的话题
        String hotTopicKey = PrefixKeyConstant.BLOG_HOT_TOPIC_KEY;
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();
        //还原保存在redis中的话题数据，用来匹配删除话题在热搜key中值
        TopicTitleVo topicTitleVo = new TopicTitleVo(id,topicTitle);
        String str = JSON.toJSONString(topicTitleVo);
        //匹配删除
        zSet.remove(hotTopicKey,str);
    }

    //启用话题
    @Transactional
    @Override
    public void enableTopicById(String id, String topicTitle) {
        //启用话题
        BlogTopic blogTopic = new BlogTopic();
        blogTopic.setId(id);
        blogTopic.setStatus((byte) 1);
        baseMapper.updateById(blogTopic);

        //恢复该话题的热度，将该话题的热度加入热搜缓存中
        String hotTopicKey = PrefixKeyConstant.BLOG_HOT_TOPIC_KEY;
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();
        //还原保存在redis中的话题数据，用来匹配删除话题在热搜key中值
        TopicTitleVo topicTitleVo = new TopicTitleVo(id,topicTitle);
        String str = JSON.toJSONString(topicTitleVo);

        //计算热度
        BlogTopic topic = baseMapper.selectById(id);
        //总讨论数 TODO 要加上缓存中的讨论数修改数
        int totalDiscussion = topic.getTotalDiscussion();
        //相差小时数=当前时间 - 创建时间
        LocalDateTime createTime = topic.getCreateTime();
        LocalDateTime dateTimeNow=LocalDateTime.now();
        Duration duration = Duration.between(createTime, dateTimeNow);
        long diffHours = duration.toHours();//相差小时
        //计算热度
        double hotness=(double)totalDiscussion/Math.pow(diffHours+2,1.5);

        zSet.add(hotTopicKey,str,hotness);
    }
}
