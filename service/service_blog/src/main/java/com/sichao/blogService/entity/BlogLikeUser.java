package com.sichao.blogService.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * <p>
 * 用户点赞博客关系表
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("blog_like_user")
@Schema(name = "BlogLikeUser对象", description = "用户点赞博客关系表")
public class BlogLikeUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户点赞博客关系id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "博客id")
    @TableField("blog_id")
    private String blogId;

    @Schema(description = "用户id")
    @TableField("user_id")
    private String userId;

    @Schema(description = "状态，1表示已点赞，0表示取消点赞，（使用status字段表示点赞状态，避免频繁插入、删除数据）")
    @TableField("status")
    private Boolean status;

    @Schema(description = "是否逻辑删除：1（true）、0（false），默认为0")
    @TableLogic//指定对应字段做逻辑删除操作
    @TableField("is_deleted")
    private Byte isDeleted;

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
