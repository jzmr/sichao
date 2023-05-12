package com.sichao.blogService.entity;

import com.baomidou.mybatisplus.annotation.*;

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

    @Schema(description = "图片地址1")
    @TableField("img_one")
    private String imgOne;

    @Schema(description = "图片地址2")
    @TableField("img_Two")
    private String imgTwo;

    @Schema(description = "图片地址3")
    @TableField("img_Three")
    private String imgThree;

    @Schema(description = "图片地址4")
    @TableField("img_Four")
    private String imgFour;


    @Schema(description = "状态，1表示可用，0表示禁用")
    @TableField("`status`")
    private Byte status;

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
