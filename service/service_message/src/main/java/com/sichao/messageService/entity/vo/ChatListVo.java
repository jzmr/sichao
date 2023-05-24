package com.sichao.messageService.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description: 聊天列表vo
 * @author: sjc
 * @createTime: 2023年05月23日 21:25
 */
@Data
@Schema(name = "聊天列表vo", description = "聊天列表vo")
public class ChatListVo {
    @Schema(description = "聊天列表id")
    private String id;

    @Schema(description = "用户聊天关系id")
    private String linkId;

    @Schema(description = "发送方用户id")
    private String fromUserId;

    @Schema(description = "接收方用户id")
    private String toUserId;

    @Schema(description = "发送方是否在窗口")
    private Boolean fromWindow;

    @Schema(description = "接收方是否在窗口")
    private Boolean toWindow;

    @Schema(description = "未读数(最大为99)")
    private Byte unreadCount;

    @Schema(description = "状态（0-发送方未删除列表 1-已删除）")
    private Boolean status;

    @Schema(description = "昵称(最少2位、最多8位)")
    private String nickname;

    @Schema(description = "头像url")
    private String avatarUrl;

    @Schema(description = "最后一条消息内容")
    private String lastMessage;

    @Schema(description = "最后一条消息的创建时间")
    private LocalDateTime createTime;
}
