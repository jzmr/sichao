package com.sichao.userService.service;

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
    UserInfoVo getUserInfoByToken(long id);
    //注销
    void logout(String token);
    //查看用户是否被禁用
    boolean userIsDisabled(String userId);
    //修改密码
    void updatePassword(String token,String userId, UpdatePasswordVo updatePasswordVo);
    //根据用户id查看用户信息（密码除外）
    UserInfoVo getUserInfoById(long id);
    //修改用户个人信息（头像、密码除外）
    void updateInfo(long userId, UpdateInfoVo updateInfoVo);
}
