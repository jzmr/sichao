package com.sichao.messageService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description: 聊天信息vo
 * @author: sjc
 * @createTime: 2023年05月24日 20:56
 */
@Data
@Schema(name = "聊天信息vo", description = "聊天信息vo")
public class ChatMessageVo {
    @Schema(description = "聊天内容详情id")
    private String id;

    @Schema(description = "发送方用户id")
    private String fromUserId;

    @Schema(description = "消息内容(最多500字)")
    private String content;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
