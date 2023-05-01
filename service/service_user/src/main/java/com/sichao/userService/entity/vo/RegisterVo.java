package com.sichao.userService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

//Vo表现层对象类
@Data
@Schema(name="注册对象", description="注册对象")
public class RegisterVo {
    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "验证码")
    private String code;
}
