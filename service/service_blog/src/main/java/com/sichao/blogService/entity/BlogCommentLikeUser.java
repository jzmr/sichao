package com.sichao.blogService.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * <p>
 * 用户点赞评论关系表
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("blog_comment_like_user")
@Schema(name = "BlogCommentLikeUser对象", description = "用户点赞评论关系表")
public class BlogCommentLikeUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户点赞评论关系id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "评论id")
    @TableField("comment_id")
    private String commentId;

    @Schema(description = "用户id")
    @TableField("user_id")
    private String userId;

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
