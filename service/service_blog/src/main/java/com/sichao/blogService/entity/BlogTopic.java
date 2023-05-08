package com.sichao.blogService.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * <p>
 * 话题表
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("blog_topic")
@Schema(name = "BlogTopic对象", description = "话题表")
public class BlogTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "话题id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "话题名称(最多25字)")
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
    @TableLogic//指定对应字段做逻辑删除操作
    @TableField("is_deleted")
    private boolean isDeleted;

    @Schema(description = "版本号（乐观锁操作要用到）")
    @Version//指定对应字段做乐观锁操作
    @TableField("version")
    private Integer version;

    @Schema(description = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)//插入数据时执行填充
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)//插入或修改数据时执行填充
    private LocalDateTime updateTime;
}
