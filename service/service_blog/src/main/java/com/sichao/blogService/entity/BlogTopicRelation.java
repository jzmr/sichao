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
 * 话题与博客关系表
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Getter
@Setter
@TableName("blog_topic_relation")
@Schema(name = "BlogTopicRelation对象", description = "话题与博客关系表")
public class BlogTopicRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "博客与话题关系id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "话题id")
    @TableField("topic_id")
    private Long topicId;

    @Schema(description = "博客id")
    @TableField("blog_id")
    private Long blogId;

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
