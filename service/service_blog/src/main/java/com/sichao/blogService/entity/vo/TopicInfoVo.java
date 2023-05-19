package com.sichao.blogService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 话题信息VO
 * @author: sjc
 * @createTime: 2023年05月08日 22:16
 */
@Data
@Schema(name = "话题信息VO", description = "话题信息VO")
public class TopicInfoVo {
    @Schema(description = "话题id")
    private String id;

    @Schema(description = "话题名称(最多25字)")
    private String topicTitle;

    @Schema(description = "话题描述(最多1000字)")
    private String topicDescription;

    @Schema(description = "话题图标URL")
    private String iconUrl;

    @Schema(description = "话题创建者ID")
    private String creatorId;

    @Schema(description = "话题创建者昵称")
    private String creatorNickname;

    @Schema(description = "话题创建者头像")
    private String creatorAvatarUrl;

}
