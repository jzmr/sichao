package com.sichao.userService.entity;

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
 * 用户订阅话题关系表
 * </p>
 *
 * @author jicong
 * @since 2023-04-28
 */
@Getter
@Setter
@TableName("user_subscription_topic")
@Schema(name = "UserSubscriptionTopic对象", description = "用户订阅话题关系表")
public class UserSubscriptionTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户订阅话题关系id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "话题id")
    @TableField("topic_id")
    private Long topicId;

    @Schema(description = "状态，1表示已订阅，0表示取消订阅（使用status字段表示订阅状态，避免频繁插入、删除数据）")
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
