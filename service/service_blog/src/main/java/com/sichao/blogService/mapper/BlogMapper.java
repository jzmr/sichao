package com.sichao.blogService.mapper;

import com.sichao.blogService.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sichao.blogService.entity.vo.BlogVo;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
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
    //查询博客vo信息
    BlogVo getBlogVoInfo(String blogId);
    //查询用户关注的用户的博客(根据创建时间倒序查询前200条)
    List<Blog> getFollowingBlog(String userId, LocalDateTime dateTime, int limit);
}
