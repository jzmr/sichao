package com.sichao.blogService.mapper;

import com.sichao.blogService.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sichao.blogService.entity.vo.BlogVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 博客表 Mapper 接口
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    //查询指定话题id下的博客
    List<BlogVo> getBlogByTopicId(String userId, String topicId);
    //查询指定话题id下的实时博客
    List<BlogVo> getRealTimeBlogByTopicId(String userId, String topicId);
}
