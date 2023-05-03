package com.sichao.userService.entity;

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
 * 用户关注用户关系id
 * </p>
 *
 * @author jicong
 * @since 2023-05-03
 */

@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("user_follow")
@Schema(name = "UserFollow对象", description = "用户关注用户关系表")
public class UserFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户关注用户关系id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)//设置主键策略
    private String id;

    @Schema(description = "粉丝id，即发起关注的用户id")
    @TableField("follower_id")
    private String followerId;

    @Schema(description = "关注id，即被关注的用户id")
    @TableField("following_id")
    private String followingId;

    @Schema(description = "状态，1表示已关注，0表示取消关注（使用status字段表示关注状态，避免频繁插入、删除数据）")
    @TableField("`status`")
    private Boolean status;

    @Schema(description = "是否逻辑删除：1（true）、0（false），默认为0")
    @TableField("is_deleted")
    private Boolean isDeleted;

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
