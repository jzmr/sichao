package com.sichao.blogService.mapper;

import com.sichao.blogService.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

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

}
