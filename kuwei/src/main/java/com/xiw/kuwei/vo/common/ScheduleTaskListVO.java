package com.xiw.kuwei.vo.common;

import com.diboot.core.binding.annotation.BindDict;
import com.diboot.core.binding.annotation.BindField;
import com.diboot.core.vo.LabelValue;
import com.diboot.iam.entity.IamUser;
import com.xiw.kuwei.entity.common.ScheduleTask;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 定时任务 VO定义
 * @author xiw
 * @version 1.0
 * @date 2026-05-08
 * Copyright © xiwwwwww
 */
@Getter
@Setter
@Accessors(chain = true)
public class ScheduleTaskListVO extends ScheduleTask {

    private static final long serialVersionUID = 1329949083974429386L;

    /**
     * 任务类型 关联字典选项
     */
    @BindDict(type = "TASK_TYPE", field = "taskType")
    private LabelValue taskTypeLabel;

    /**
     * 执行类型 关联字典选项
     */
    @BindDict(type = "EXE_TYPE", field = "executionType")
    private LabelValue executionTypeLabel;

    /**
     * 任务状态 关联字典选项
     */
    @BindDict(type = "TASK_STATUS", field = "taskStatus")
    private LabelValue taskStatusLabel;

    /**
     * 创建人 关联信息
     */
    @BindField(entity = IamUser.class, field = "realname", condition = "this.create_by = id")
    private String createByLabel;

    /**
     * 更新人 关联信息
     */
    @BindField(entity = IamUser.class, field = "realname", condition = "this.update_by = id")
    private String updateByLabel;

    /**
     * 创建者 关联信息
     */
    @BindField(entity = IamUser.class, field = "realname", condition = "this.related_account_id = id")
    private String relatedAccountIdLabel;
}
