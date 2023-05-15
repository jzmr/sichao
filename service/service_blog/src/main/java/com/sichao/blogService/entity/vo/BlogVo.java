package com.sichao.blogService.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: 博客vo，用来在前端页面展示博客、用户信息
 * @author: sjc
 * @createTime: 2023年05月10日 23:40
 */
@Data
@Schema(name = "博客vo", description = "博客vo")
public class BlogVo {

    @Schema(description = "博客id")
    private String id;

    @Schema(description = "博客内容")
    private String content;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "图片地址1")
    private String imgOne;

    @Schema(description = "图片地址2")
    private String imgTwo;

    @Schema(description = "图片地址3")
    private String imgThree;

    @Schema(description = "图片地址4")
    private String imgFour;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "图片集合，将不为空的图片地址放进这个集合，是为了方便前端可以大图预览图片")
    private List<String> imgList;

    @Schema(description = "当前用户是否点赞该博客，未登录则默认为点赞")
    private boolean isLikeByCurrentUser;//前端中会吞掉is，保存likeByCurrentUser



    //用户信息
    @Schema(description = "创建博客用户id")
    private String creatorId;

    @Schema(description = "昵称(最少2位、最多8位)")
    private String nickname;

    @Schema(description = "头像url")
    private String avatarUrl;



}
