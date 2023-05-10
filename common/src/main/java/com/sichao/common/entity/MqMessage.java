package com.sichao.common.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * <p>
 * MQ消息表
 * </p>
 *
 * @author jicong
 * @since 2023-05-10
 */
@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("mq_message")
@Schema(name = "MqMessage对象", description = "MQ消息表")
public class MqMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "MQ消息id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "消息内容，转换成JSON字符串的形式保存")
    @TableField("content")
    private String content;

    @Schema(description = "交换机")
    @TableField("to_exchange")
    private String toExchange;

    @Schema(description = "路由")
    @TableField("routing_key")
    private String routingKey;

    @Schema(description = "记录信息本来的类型，")
    @TableField("class_type")
    private String classType;

    @Schema(description = "状态：0-新建 1-已发送 2-错误抵达 3-已消费")
    @TableField("`status`")
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

    public MqMessage(String content, String toExchange, String routingKey, String classType, Byte status) {
        this.content = content;
        this.toExchange = toExchange;
        this.routingKey = routingKey;
        this.classType = classType;
        this.status = status;
    }
}
