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
    //查询博客vo信息
    BlogVo getBlogVoInfo(String blogId);
}
