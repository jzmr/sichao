package com.sichao.userService.mapper;

import com.sichao.userService.entity.UserFollow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

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

}
