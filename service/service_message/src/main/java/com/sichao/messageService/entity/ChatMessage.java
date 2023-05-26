package com.sichao.messageService.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * <p>
 * 聊天内容详情表
 * </p>
 *
 * @author jicong
 * @since 2023-05-23
 */
@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("chat_message")
@Schema(name = "ChatMessage对象", description = "聊天内容详情表")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "聊天内容详情id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)//设置主键策略
    private String id;

    @Schema(description = "用户聊天关系id")
    @TableField("link_id")
    private String linkId;

    @Schema(description = "发送方用户id")
    @TableField("from_user_id")
    private String fromUserId;

    @Schema(description = "接收方用户id")
    @TableField("to_user_id")
    private String toUserId;

    @Schema(description = "消息内容(最多500字)")
    @TableField("content")
    private String content;

    @Schema(description = "类型")
    @TableField("`type`")
    private Byte type;

    @Schema(description = "是否是最后一条消息")
    @TableField("is_latest")
    private Boolean isLatest;

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

    public ChatMessage(String linkId, String fromUserId, String toUserId, String content) {
        this.linkId = linkId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.content = content;
    }
}
