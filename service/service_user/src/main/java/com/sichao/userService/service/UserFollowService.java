package com.sichao.userService.service;

import com.sichao.userService.entity.UserFollow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sichao.userService.entity.vo.FollowListVo;

import java.util.List;
import java.util.Set;

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
    //查看用户关注列表
    List<FollowListVo> getFollowingList(String currentId, String id);
    //查看用户粉丝列表
    List<FollowListVo> getFollowerList(String currentId, String id);
    //查询当前用户关注的用户的昵称
    List<String> getFollowingNicknameList(String userId);
    //获取用户关注列表
    Set<String> getFollowingSetCache(String userId);
    //获取用户粉丝列表
    Set<String> getFollowerSetCache(String userId);
}
