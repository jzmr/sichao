package com.sichao.userService.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 关注、粉丝列表vo
 * @author: sjc
 * @createTime: 2023年05月06日 10:46
 *
 * 可以封装查看某位用户的关注列表，也可以封装查看某位用户的粉丝列表
 * 并有判断当前用户是否关注了列表中的某些用户
 */
@Data
@Schema(name = "关注、粉丝列表vo", description = "关注、粉丝列表vo")
public class FollowListVo {

    @Schema(description = "用户id")
    private String id;

    @Schema(description = "昵称(最少2位、最多8位)")
    private String nickname;

    @Schema(description = "性别：0-男 1-女 2-未知")
    private Byte gender;

    @Schema(description = "头像url")
    private String avatarUrl;

    @Schema(description = "个人签名(最多255个字符)")
    private String sign;

    @Schema(description = "当前用户是否关注该用户")
    private boolean currentIdIsFollow;
}
