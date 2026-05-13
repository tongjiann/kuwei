package com.xiw.kuwei.service.common.impl;

import com.diboot.core.service.impl.BaseServiceImpl;
import com.xiw.kuwei.entity.common.ScheduleTask;
import com.xiw.kuwei.helper.ScheduleTaskHelper;
import com.xiw.kuwei.mapper.common.ScheduleTaskMapper;
import com.xiw.kuwei.service.common.ScheduleTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;


/**
* 定时任务 相关Service实现类
* @author xiw
* @version 1.0
* @date 2026-05-08
* Copyright © xiwwwwww
*/
@Slf4j
@Service
public class ScheduleTaskServiceImpl extends BaseServiceImpl<ScheduleTaskMapper, ScheduleTask> implements ScheduleTaskService {

    @Override
    protected void afterBatchCreate(Collection<ScheduleTask> entityList) {
        ScheduleTaskHelper.refresh();
        super.afterBatchCreate(entityList);
    }

    @Override
    protected void afterCreate(ScheduleTask entity) {
        ScheduleTaskHelper.refresh();
        super.afterCreate(entity);
    }

    @Override
    protected void afterDelete(Object entityIds) {
        ScheduleTaskHelper.refresh();
        super.afterDelete(entityIds);
    }

    @Override
    protected void afterDelete(String fieldKey, Object fieldVal) {
        ScheduleTaskHelper.refresh();
        super.afterDelete(fieldKey, fieldVal);
    }

    @Override
    protected void afterUpdate(ScheduleTask entity) {
        ScheduleTaskHelper.refresh();
        super.afterUpdate(entity);
    }

}