package com.sichao.userService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 修改密码Vo类
 * @author: sjc
 * @createTime: 2023年05月02日 16:12
 */
@Data
@Schema(name="修改密码对象", description="修改密码对象")
public class UpdatePasswordVo {
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "验证码")
    private String code;

    @Schema(description = "原密码")
    private String oldPassword;

    @Schema(description = "新密码")
    private String newPassword;
}
