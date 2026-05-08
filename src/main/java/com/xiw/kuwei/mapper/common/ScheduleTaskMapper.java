package com.xiw.kuwei.mapper.common;

import com.diboot.core.mapper.BaseCrudMapper;
import com.xiw.kuwei.entity.common.ScheduleTask;
import org.apache.ibatis.annotations.Mapper;

/**
* 定时任务 相关Mapper层接口定义
* @author xiw
* @version 1.0
* @date 2026-05-08
* Copyright © xiwwwwww
*/
@Mapper
public interface ScheduleTaskMapper extends BaseCrudMapper<ScheduleTask> {

}