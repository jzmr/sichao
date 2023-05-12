package com.sichao.userService.mapper;

import com.sichao.userService.entity.UserFollow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sichao.userService.entity.vo.FollowListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户关注用户关系id Mapper 接口
 * </p>
 *
 * @author jicong
 * @since 2023-05-03
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    //查看用户关注列表
    List<FollowListVo> getFollowingList(@Param("currentId") String currentId, @Param("id") String id);
    //查看用户粉丝列表
    List<FollowListVo> getFollowerList(String currentId, String id);
    //查询当前用户关注的用户的昵称
    List<String> getFollowingNicknameList(String userId);
}
