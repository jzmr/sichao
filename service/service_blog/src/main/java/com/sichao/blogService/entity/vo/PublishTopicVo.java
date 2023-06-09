package com.sichao.blogService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * @Description: 发布话题VO，用于与发布话题
 * @author: sjc
 * @createTime: 2023年05月08日 13:23
 */
@Data
@Schema(name = "发布话题VO", description = "发布话题VO")
public class PublishTopicVo {

    @Schema(description = "话题名称(最多25字)")
    private String topicTitle;

    @Schema(description = "话题描述(最多1000字)")
    private String topicDescription;

    @Schema(description = "话题图标URL")
    private String iconUrl;

    @Schema(description = "话题创建者ID")
    private String creatorId;
}

