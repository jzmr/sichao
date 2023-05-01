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
 * 评论与子评论表
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Getter
@Setter
@TableName("blog_comment")
@Schema(name = "BlogComment对象", description = "评论与子评论表")
public class BlogComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "评论id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "博客id")
    @TableField("blog_id")
    private Long blogId;

    @Schema(description = "父评论id（无父评论时为0）")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "用户id")
    @TableField("creator_id")
    private Long creatorId;

    @Schema(description = "评论内容")
    @TableField("comment_content")
    private String commentContent;

    @Schema(description = "子评论数")
    @TableField("comment_count")
    private Integer commentCount;

    @Schema(description = "点赞数")
    @TableField("like_count")
    private Integer likeCount;

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
