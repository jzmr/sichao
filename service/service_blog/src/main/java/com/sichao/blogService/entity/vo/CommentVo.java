package com.sichao.blogService.entity.vo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description: 评论vo，用来在前端页面展示博客、用户信息
 * @author: sjc
 * @createTime: 2023年05月14日 12:53
 */
@Data
@Schema(name = "评论vo", description = "评论vo")
public class CommentVo {

    @Schema(description = "评论id")
    private String id;

    @Schema(description = "博客id")
    private String blogId;

    @Schema(description = "评论内容")
    private String commentContent;

//    @Schema(description = "点赞数")
//    private Integer likeCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

//    @Schema(description = "当前用户是否点赞该评论，未登录则默认为点赞")
//    private boolean likeByCurrentUser;



    //用户信息
    @Schema(description = "用户id")
    private String creatorId;

    @Schema(description = "昵称(最少2位、最多8位)")
    private String nickname;

    @Schema(description = "头像url")
    private String avatarUrl;
}
