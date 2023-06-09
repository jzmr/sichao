package com.sichao.blogService.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.client.UserClient;
import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.entity.vo.PublishTopicVo;
import com.sichao.blogService.entity.vo.TopicInfoVo;
import com.sichao.blogService.entity.vo.TopicTitleVo;
import com.sichao.blogService.mapper.BlogTopicMapper;
import com.sichao.blogService.service.BlogTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.entity.to.UserInfoTo;
import com.sichao.common.exceptionhandler.sichaoException;
import com.sichao.common.utils.R;
import io.swagger.v3.core.util.Json;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    @Autowired
    private UserClient userClient;


    //发布话题
    @Transactional
    @Override
    public void publishTopic(PublishTopicVo publishTopicVo) {
        //校验话题title是否合法
        if(!StringUtils.hasText(publishTopicVo.getTopicTitle())){
            throw new sichaoException(Constant.FAILURE_CODE,"话题标题不能为空");
        }
        //清除话题title的前导后导空格
        publishTopicVo.setTopicTitle(publishTopicVo.getTopicTitle().trim());
        QueryWrapper<BlogTopic> wrapper = new QueryWrapper<>();
        wrapper.eq("topic_title",publishTopicVo.getTopicTitle());
        BlogTopic one = baseMapper.selectOne(wrapper);
        if(one!=null)throw new sichaoException(Constant.FAILURE_CODE,"该话题已存在");

        //保存话题
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
        String hotTopicKey = PrefixKeyConstant.BLOG_HOT_TOPIC_KEY;//热搜榜key
        String hotTopicTempKey = PrefixKeyConstant.BLOG_HOT_TOPIC_TEMP_KEY;//临时热搜榜key
        //根据话题热度倒序（从大到小）获取50条话题
        //保存在redis中的zSet类型中的数据，默认是升序，查询时在命令的Z后面添加REV可降序查询
        Set<String> set=null;
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();

        //避免因为热搜排行更新期间用户无法查询热搜
        if(stringRedisTemplate.keys(hotTopicKey)!=null && !stringRedisTemplate.keys(hotTopicKey).isEmpty()){
            set = zSet.reverseRange(hotTopicKey, 0, 49);
        }else if(stringRedisTemplate.keys(hotTopicTempKey)!=null  && !stringRedisTemplate.keys(hotTopicTempKey).isEmpty()){
            //去临时热搜榜查，因为可能是在更新热搜榜之前删除热搜榜的key导致无法获取，而此时临时热搜榜是有数据的，所以可以在这里查
            set=zSet.reverseRange(hotTopicTempKey, 0, 49);
        }
        if(set==null) return null;

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
        //根据用户id获取用户信息(昵称、头像)
        R r = userClient.getUserById(blogTopic.getCreatorId());
        String jsonString = JSON.toJSONString(r.getData().get("userInfoTo"));
        UserInfoTo userInfoTo = JSON.parseObject(jsonString, UserInfoTo.class);
        topicInfo.setCreatorNickname(userInfoTo.getNickname());
        topicInfo.setCreatorAvatarUrl(userInfoTo.getAvatarUrl());
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
        //总讨论数
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
