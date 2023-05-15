package com.sichao.blogService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 发布评论Vo
 * @author: sjc
 * @createTime: 2023年05月14日 00:55
 */
@Data
@Schema(name = "发布评论Vo", description = "发布评论Vo")
public class PublishCommentVo {

    @Schema(description = "博客id")
    private String blogId;

    @Schema(description = "评论内容")
    private String content;

}
