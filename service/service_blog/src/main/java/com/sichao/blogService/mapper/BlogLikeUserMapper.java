package com.sichao.blogService.mapper;

import com.sichao.blogService.entity.BlogLikeUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户点赞博客关系表 Mapper 接口
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Mapper
public interface BlogLikeUserMapper extends BaseMapper<BlogLikeUser> {

}
