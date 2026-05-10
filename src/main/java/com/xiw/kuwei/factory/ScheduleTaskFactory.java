package com.xiw.kuwei.factory;

import com.xiw.kuwei.entity.common.ScheduleTask;
import com.xiw.kuwei.exception.LogicalException;
import com.xiw.kuwei.task.AbstractTask;
import com.xiw.kuwei.task.DailySignalInfoTask;
import com.xiw.kuwei.task.DailySyncStockInfoTask;
import com.xiw.kuwei.task.MessageTask;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.xiw.kuwei.constant.ScheduleTaskConstant.*;


/**
 * @author xiwang
 * @apiNote
 * @since 2022-10-18 13:45
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScheduleTaskFactory {

    public static AbstractTask getAbstractTask(ScheduleTask scheduleTask) {
        Integer taskType = Integer.parseInt(scheduleTask.getTaskType());
        if (taskType == null) {
            throw new LogicalException(scheduleTask.getId() + "未知的定时任务类型");
        }
        return switch (taskType) {
            case TASK_TYPE_MESSAGE -> new MessageTask(scheduleTask);
            case TASK_TYPE_DAILY_STOCK_SIGNAL -> new DailySignalInfoTask(scheduleTask);
            case TASK_TYPE_DAILY_SYNC_STOCK_INFO -> new DailySyncStockInfoTask(scheduleTask);
            default -> throw new LogicalException("未知的定时任务类型");
        };
    }

}
