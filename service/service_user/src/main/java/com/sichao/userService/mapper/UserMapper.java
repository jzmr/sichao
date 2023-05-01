package com.sichao.userService.mapper;

import com.sichao.userService.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author jicong
 * @since 2023-04-28
 */
public interface UserMapper extends BaseMapper<User> {

    public boolean userIsDisabled(String userId);
}
