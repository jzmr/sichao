package com.sichao.blogService.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 发布博客VO
 * @author: sjc
 * @createTime: 2023年05月09日 15:20
 */
@Data
@Schema(name = "发布博客VO", description = "发布博客VO")
public class PublishBlogVo {

    @Schema(description = "博客内容")
    private String content;

    @Schema(description = "创建博客用户id")
    private String creatorId;

    @Schema(description = "图片地址1")
    private String imgOne;

    @Schema(description = "图片地址2")
    private String imgTwo;

    @Schema(description = "图片地址3")
    private String imgThree;

    @Schema(description = "图片地址4")
    private String imgFour;

}
