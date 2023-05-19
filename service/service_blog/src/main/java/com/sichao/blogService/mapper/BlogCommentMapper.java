package com.sichao.blogService.mapper;

import com.sichao.blogService.entity.BlogComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sichao.blogService.entity.vo.CommentVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 评论与子评论表 Mapper 接口
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Mapper
public interface BlogCommentMapper extends BaseMapper<BlogComment> {
    //查询指定博客id下的评论与其作者信息(升序)
    List<CommentVo> getCommentByBlogId(String blogId);
    //根据评论id查询评论vo信息
    CommentVo getCommentVoInfo(String commentId);
}
