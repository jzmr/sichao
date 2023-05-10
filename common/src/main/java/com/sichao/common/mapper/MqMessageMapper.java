package com.sichao.common.mapper;

import com.sichao.common.entity.MqMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * MQ消息表 Mapper 接口
 * </p>
 *
 * @author jicong
 * @since 2023-05-10
 */
@Mapper
public interface MqMessageMapper extends BaseMapper<MqMessage> {

}
