package com.sichao.blogService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.blogService.client.UserClient;
import com.sichao.blogService.entity.Blog;
import com.sichao.blogService.entity.BlogTopic;
import com.sichao.blogService.entity.vo.PublishBlogVo;
import com.sichao.blogService.mapper.BlogMapper;
import com.sichao.blogService.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.blogService.service.BlogTopicService;
import com.sichao.common.constant.Constant;
import com.sichao.common.exceptionhandler.sichaoException;
import com.sichao.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 博客表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    @Autowired
    private BlogTopicService blogTopicService;
    @Autowired
    private UserClient userClient;


    //发布博客
    @Transactional
    @Override
    public void saveBlog(PublishBlogVo publishBlogVo) {
        String content = publishBlogVo.getContent();//获取博客内容
        if(!StringUtils.hasText(content))
            throw new sichaoException(Constant.FAILURE_CODE,"博客内容不能为空");
        List<String> topicList=new ArrayList<>();//用来保存话题id的集合
        List<String> userList=new ArrayList<>();//用来保存用户id的集合
        StringBuffer strb=new StringBuffer();//用来拼接博客内容
        int idx = 0;

        /**
         * 1、话题title与用户昵称唯一的，且都是没有前导或后导空格的
         * 2、必须是先匹配@用户，再匹配#话题#，不然会有bug（ #s @sad# ）,拼接
         *    好@用户的前中缀之后的长度会大于25，是的#话题#的正则表达式不会生效
         */
        //使用正则表达式获取博客中被@的用户（@用户 ）
        //匹配以@开头，后面跟随着至少一个非'@'、非空格字符,最后匹配零个或一个空格
        Pattern referer_pattern_user = Pattern.compile("@([^@^\\s]{1,})([\\s]{0,1})");
        Matcher matchr_user = referer_pattern_user.matcher(content);
        //之前字符串中匹配到的位置不会在被匹配到，会往后开始匹配，配合while循环做到匹配整个字符串中所有符合正则表达式的子串
        while (matchr_user.find()){//为true说明匹配，为false说明不匹配
            String origion_str_user = matchr_user.group();//获取匹配到的字符串
            String userStr = origion_str_user.substring(1, origion_str_user.length()).trim();//裁剪

            //TODO 这里的循环查库优化？
            R r = (R) userClient.getUserIdByNickname(userStr);//远程调用查询用户id
            String userId = (String) r.getData().get("userId");

            //给@用户添加超链接
            //matchr.start()：获得被匹配到的子串在原串的起始位置
            strb.append(content.substring(idx, matchr_user.start()));
            if(userId!=null){
                userList.add(userId);//添加用户id到集合
                strb.append(Constant.BLOG_AT_USER_HYPERLINK_PREFIX)
                        .append(userId)
                        .append(Constant.BLOG_AT_USER_HYPERLINK_INFIX);
            }
            strb.append(origion_str_user);
            if(userId!=null){
                strb.append(Constant.BLOG_AT_USER_HYPERLINK_SUFFIX);
            }
            idx=matchr_user.start()+origion_str_user.length();
        }
        strb.append(content.substring(idx));

        content=strb.toString();//重新赋值
        strb.delete(0,strb.length());//清空

        //使用正则表达式获取话题（ #话题# ）
        //正则表达式：^ {1}#{1}[^# ]{1,25}#{1} {1}$
        // 5~29个字符，第一个与最后一个字符必须是空格，第二个与倒数第二个字符必须是'#'，其余的字符只能是不能为'#',
        //下面的代码不能有^与$,加了会对整个字符串进行匹配，如果字符串不是以#号开头或结尾则不匹配
        //不加则可以用来对字符串中每个子串都进行匹配判断
        Pattern referer_pattern_topic = Pattern.compile(" {1}#{1}[^# ]{1,25}#{1} {1}");
        Matcher matchr_topic = referer_pattern_topic.matcher(content);
        idx=0;
        //之前字符串中匹配到的位置不会在被匹配到，会往后开始匹配，配合while循环做到匹配整个字符串中所有符合正则表达式的子串
        while (matchr_topic.find()){//为true说明匹配，为false说明不匹配。
            String origion_str_topic = matchr_topic.group();//获取匹配到的字符串
            String topicStr = origion_str_topic.substring(2, origion_str_topic.length()-2).trim();//裁剪

            //TODO 这里的循环查库优化？抽取变量？
            QueryWrapper<BlogTopic> wrapperTopic = new QueryWrapper<>();
            wrapperTopic.eq("topic_title",topicStr);
            BlogTopic blogTopic = blogTopicService.getOne(wrapperTopic);//查询话题id

            //给#话题#添加超链接
            //matchr.start()：获得被匹配到的子串在原串的起始位置
            strb.append(content.substring(idx, matchr_topic.start()));
            if(blogTopic!=null){
                topicList.add(blogTopic.getId());//添加话题id到集合
                strb.append(Constant.BLOG_AT_TOPIC_HYPERLINK_PREFIX)
                        .append(blogTopic.getId())
                        .append(Constant.BLOG_AT_TOPIC_HYPERLINK_INFIX);
            }
            strb.append(origion_str_topic);
            if(blogTopic!=null) {
                strb.append(Constant.BLOG_AT_TOPIC_HYPERLINK_SUFFIX);
            }

            idx=matchr_topic.start()+origion_str_topic.length();
        }
        strb.append(content.substring(idx));


        // TODO RabbitMQ发送消息，异步实现对@用户与#话题#的处理


        //保存博客
        Blog blog = new Blog();
        blog.setContent(strb.toString());
        blog.setCreatorId(publishBlogVo.getCreatorId());
        blog.setImageUrl(publishBlogVo.getImageUrl());
        baseMapper.insert(blog);
    }
}
