package com.sichao.userService.service;

import com.sichao.common.entity.to.UserInfoTo;
import com.sichao.userService.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.userService.entity.vo.RegisterVo;
import com.sichao.userService.entity.vo.UpdateInfoVo;
import com.sichao.userService.entity.vo.UpdatePasswordVo;
import com.sichao.userService.entity.vo.UserInfoVo;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-04-28
 */

public interface UserService extends IService<User> {
    //注册
    void register(RegisterVo registerVo);
    //登录
    String login(User user);
    //根据token信息获取用户信息（密码除外）
    UserInfoVo getUserInfoByToken(String id);
    //注销
    void logout(String token);
    //查看用户是否被禁用
    boolean userIsDisabled(String userId);
    //修改密码
    void updatePassword(String token,String userId, UpdatePasswordVo updatePasswordVo);
    //根据用户id查看用户信息（密码除外）
    UserInfoVo getUserInfoById(String id);
    //修改头像url
    void updateAvatarUrl(String userId, String avatarUrl);
    //修改用户个人信息（头像、密码除外）
    void updateInfo(String userId, UpdateInfoVo updateInfoVo);

    //根据昵称（用户名）查询用户id
    String getUserIdByNickname(String nickname);
    //根据用户id查询用户信息
    UserInfoTo getUserById(String id);
    //根据用户id对其博客数+1
    void userBlogCountPlusOne(String id);
    //根据用户id对其博客数-1
    void userBlogCountMinusOne(String id);
}
