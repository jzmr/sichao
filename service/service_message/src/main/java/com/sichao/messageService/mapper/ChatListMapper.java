package com.sichao.messageService.mapper;

import com.sichao.messageService.entity.ChatList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 聊天列表表 Mapper 接口
 * </p>
 *
 * @author jicong
 * @since 2023-05-23
 */
@Mapper
public interface ChatListMapper extends BaseMapper<ChatList> {

}
