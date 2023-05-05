package com.sichao.common.mapper;

import com.sichao.common.entity.TaskExecutionInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务给你需要用到的mapper
 * <p>
 * 任务执行信息表 Mapper 接口
 * </p>
 *
 * @author jicong
 * @since 2023-05-05
 */
@Mapper
public interface TaskExecutionInfoMapper extends BaseMapper<TaskExecutionInfo> {

}
