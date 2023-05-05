package com.sichao.userService.service;

import com.sichao.userService.entity.UserFollow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户关注用户关系id 服务类
 * </p>
 *
 * @author jicong
 * @since 2023-05-03
 */
public interface UserFollowService extends IService<UserFollow> {
    // 关注用户
    void follow(String userId, String followingId);

    // 查看当前用户是否关注某位其他用户
    boolean getFollowStatus(String userId, String id);
    //取关用户
    void unfollow(String userId, String followingId);
}
