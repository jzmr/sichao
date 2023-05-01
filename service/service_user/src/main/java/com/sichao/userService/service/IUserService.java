package com.sichao.userService.service;

import com.sichao.userService.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.userService.entity.vo.RegisterVo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-04-28
 */

public interface IUserService extends IService<User> {
    //注册
    void register(RegisterVo registerVo);
    //登录
    String login(User user);
    //注销
    void logout(String token);
    //查看用户是否被禁用
    boolean userIsDisabled(String userId);
}
