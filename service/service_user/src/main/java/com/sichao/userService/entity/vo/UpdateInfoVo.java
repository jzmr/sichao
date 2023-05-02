package com.sichao.userService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * @Description: 用户修改个人资料vo类
 * @author: sjc
 * @createTime: 2023年05月02日 21:56
 */
@Data
@Schema(name = "用户修改个人资料vo", description = "用户修改个人资料vo")
public class UpdateInfoVo {
    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称(最少2位、最多8位)")
    private String nickname;

    @Schema(description = "性别：0-男 1-女 2-未知")
    private Byte gender;

    @Schema(description = "年龄")
    private Byte age;

    @Schema(description = "个人签名(最多255个字符)")
    private String sign;

    @Schema(description = "生日")
    private LocalDate birthday;
}
