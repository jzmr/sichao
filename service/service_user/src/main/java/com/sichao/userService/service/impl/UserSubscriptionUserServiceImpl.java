package com.sichao.userService.service.impl;

import com.sichao.userService.entity.UserSubscriptionUser;
import com.sichao.userService.mapper.UserSubscriptionUserMapper;
import com.sichao.userService.service.IUserSubscriptionUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户订阅用户关系表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-28
 */
@Service
public class UserSubscriptionUserServiceImpl extends ServiceImpl<UserSubscriptionUserMapper, UserSubscriptionUser> implements IUserSubscriptionUserService {

}
