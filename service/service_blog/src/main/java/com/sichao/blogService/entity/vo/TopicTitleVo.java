package com.sichao.blogService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 话题标题VO，包含话题id与话题title
 * @author: sjc
 * @createTime: 2023年05月08日 15:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "话题标题VO", description = "话题标题VO")
public class TopicTitleVo {

    @Schema(description = "话题id")
    private String id;

    @Schema(description = "话题名称")
    private String topicTitle;

}
