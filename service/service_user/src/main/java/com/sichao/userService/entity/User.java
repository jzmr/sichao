package com.sichao.userService.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author jicong
 * @since 2023-04-28
 */
@Data//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@AllArgsConstructor//注在类上，提供类的全参构造
@NoArgsConstructor//注在类上，提供类的无参构造
@TableName("user")
@Schema(name = "User对象", description = "用户表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    //IdType.ASSIGN_ID      19位数字      Long,Integer,String(支持自动转换为 String 类型，但数值类型不支持自动转换，需精准匹配，例如返回 Long，实体主键就不支持定义为 Integer)
    //IdType.ASSIGN_UUID    32位数字+字母 String(默认不含中划线的 UUID 生成)
    @Schema(description = "用户id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)//设置主键策略
    private String id;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "密码(密码至少为8位、最多20位)")
    @TableField("password")
    private String password;

    @Schema(description = "昵称(最少2位、最多8位)")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "性别：0-男 1-女 2-未知")
    @TableField("gender")
    private Byte gender;

    @Schema(description = "头像url")
    @TableField("avatar_url")
    private String avatarUrl;

    @Schema(description = "个人签名(最多255个字符)")
    @TableField("sign")
    private String sign;

    @Schema(description = "生日")
    @TableField("birthday")
    private LocalDate birthday;

    @Schema(description = "粉丝数")
    @TableField("follower_count")
    private Integer followerCount;

    @Schema(description = "订阅数(0_65535)")
    @TableField("following_count")
    private Short followingCount;

    @Schema(description = "总博客数(0_65535)")
    @TableField("blog_count")
    private Short blogCount;

    @Schema(description = "总获得点赞数")
    @TableField("total_like_count")
    private Integer totalLikeCount;

    @Schema(description = "状态，1表示可用，0表示禁用")
    @TableField("status")
    private Boolean status;

    @Schema(description = "是否逻辑删除：1（true）、0（false），默认为0")
    @TableLogic//指定对应字段做逻辑删除操作
    @TableField("is_deleted")
    private Boolean isDeleted;//数据表中的字段是tinyint且长度为1是，代码生成器生成是自动转换成boolean类型

    @Schema(description = "版本号（乐观锁操作要用到）")
    @Version//指定对应字段做乐观锁操作
    @TableField("version")
    private Integer version;

    @Schema(description = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)//插入数据时执行填充
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)//插入或修改数据时执行填充
    private LocalDateTime updateTime;
}
