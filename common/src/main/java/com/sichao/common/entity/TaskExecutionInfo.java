package com.sichao.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务给你需要用到的表
 * <p>
 * 任务执行信息表
 * </p>
 *
 * @author jicong
 * @since 2023-05-05
 */
@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("task_execution_info")
@Schema(name = "TaskExecutionInfo对象", description = "任务执行信息表")
public class TaskExecutionInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Schema(description = "任务执行信息表id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)//设置主键策略
    private String id;

    @Schema(description = "任务名称")
    @TableField("task_name")
    private String taskName;

    @Schema(description = "任务描述")
    @TableField("task_descriptions")
    private String taskDescriptions;

    @Schema(description = "任务开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "任务结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "记录异常信息：\n" +
            "1、当任务无法完成时，记录无法完成任务的原因，\n" +
            "2、处理多个数据时记录出现异常的数据id")
    @TableField("exception_info")
    private String exceptionInfo;

    @Schema(description = "状态，0表示任务进行中，1表示任务成功，2表示任务失败")
    @TableField("status")
    private Byte status;

    @Schema(description = "是否逻辑删除：1（true）、0（false），默认为0")
    @TableLogic//指定对应字段做逻辑删除操作
    @TableField("is_deleted")
    private Boolean isDeleted;

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

    //需要设置信息的构造器
    public TaskExecutionInfo(String taskName, String taskDescriptions, LocalDateTime startTime, Byte status) {
        this.taskName = taskName;
        this.taskDescriptions = taskDescriptions;
        this.startTime = startTime;
        this.status = status;
    }
}
