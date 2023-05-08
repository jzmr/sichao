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
 * 博客表
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("blog")
@Schema(name = "Blog对象", description = "博客表")
public class Blog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "博客id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "博客内容")
    @TableField("content")
    private String content;

    @Schema(description = "创建博客用户id")
    @TableField("creator_id")
    private String creatorId;

    @Schema(description = "评论数")
    @TableField("comment_count")
    private Integer commentCount;

    @Schema(description = "点赞数")
    @TableField("like_count")
    private Integer likeCount;

    @Schema(description = "多个图片地址链接成一个字符串，使用逗号分隔")
    @TableField("image_url")
    private String imageUrl;

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
