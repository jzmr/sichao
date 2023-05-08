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
 * 举报信息表
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("report_info")
@Schema(name = "ReportInfo对象", description = "举报信息表")
public class ReportInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "举报id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "发起举报的用户id")
    @TableField("user_id")
    private String userId;

    @Schema(description = "举报类型（0-用户、1-博客、2-话题。。。）")
    @TableField("report_item_type")
    private Byte reportItemType;

    @Schema(description = "被举报事物id（用户id、博客id、话题id。。。）")
    @TableField("report_item_id")
    private String reportItemId;

    @Schema(description = "举报原因（0-人身攻击，1-色情暴力，2-虚假信息。。。）")
    @TableField("report_reason")
    private Byte reportReason;

    @Schema(description = "举报详细内容")
    @TableField("report_content")
    private String reportContent;

    @Schema(description = "状态，0-未处理，1-已处理")
    @TableField("`status`")
    private Byte status;

    @Schema(description = "是否逻辑删除：1（true）、0（false），默认为0")
    @TableField("is_deleted")
    private Byte isDeleted;

    @Schema(description = "版本号（乐观锁操作要用到）")
    @TableField("version")
    private Integer version;

    @Schema(description = "创建时间（举报时间）")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
