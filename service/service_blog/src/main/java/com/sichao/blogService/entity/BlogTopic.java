package com.sichao.blogService.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 话题表
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Getter
@Setter
@TableName("blog_topic")
@Schema(name = "BlogTopic对象", description = "话题表")
public class BlogTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "话题id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "话题名称")
    @TableField("topic_title")
    private String topicTitle;

    @Schema(description = "话题描述(最多1000字)")
    @TableField("topic_description")
    private String topicDescription;

    @Schema(description = "话题图标URL")
    @TableField("icon_url")
    private String iconUrl;

    @Schema(description = "话题创建者ID")
    @TableField("creator_id")
    private String creatorId;

    @Schema(description = "话题热度")
    @TableField("topic_hot")
    private Integer topicHot;

    @Schema(description = "总讨论数，包括该话题下相关的博客数、评论数和转发数总和")
    @TableField("total_discussion")
    private Integer totalDiscussion;

    @Schema(description = "订阅话题的用户总数量")
    @TableField("total_follower")
    private Integer totalFollower;

    @Schema(description = "总浏览量")
    @TableField("total_view")
    private Integer totalView;

    @Schema(description = "状态，1表示可用，0表示禁用")
    @TableField("`status`")
    private Byte status;

    @Schema(description = "是否逻辑删除：1（true）、0（false），默认为0")
    @TableField("is_deleted")
    private Byte isDeleted;

    @Schema(description = "版本号（乐观锁操作要用到）")
    @TableField("version")
    private Integer version;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
