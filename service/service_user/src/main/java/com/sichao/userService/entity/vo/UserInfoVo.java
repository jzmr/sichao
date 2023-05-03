package com.sichao.userService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * @Description: 用户信息Vo，用来将后端查询到的用户数据做脱敏处理后封装到此vo类中传给前端。
 * @author: sjc
 * @createTime: 2023年05月02日 21:05
 */
@Data
@Schema(name = "用户信息vo", description = "用户信息vo")
public class UserInfoVo {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称(最少2位、最多8位)")
    private String nickname;

    @Schema(description = "性别：0-男 1-女 2-未知")
    private Byte gender;

    @Schema(description = "年龄")
    private Byte age;

    @Schema(description = "头像url")
    private String avatarUrl;

    @Schema(description = "个人签名(最多255个字符)")
    private String sign;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "粉丝数")
    private Integer followerCount;

    @Schema(description = "订阅数(0_65535)")
    private Short followingCount;

    @Schema(description = "总博客数(0_65535)")
    private Short blogCount;

    @Schema(description = "总点赞数")
    private Integer totalLikeCount;
}
