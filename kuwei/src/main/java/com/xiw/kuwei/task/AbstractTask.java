package com.xiw.kuwei.task;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.cron.pattern.CronPatternUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.extra.spring.SpringUtil;
import com.xiw.kuwei.entity.common.ScheduleTask;
import com.xiw.kuwei.helper.ScheduleTaskHelper;
import com.xiw.kuwei.service.common.ScheduleTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.xiw.kuwei.constant.ScheduleTaskConstant.STATUS_FINISHED;


/**
 * @author xiwang
 * @apiNote 抽象任务
 * @since 2022-10-18 13:42
 */
@Slf4j
public abstract class AbstractTask extends ScheduleTask implements Task {

    @Override
    public final void execute() {
        log.info("开始执行定时任务:{}", getTopic());

        beforeDoExecute();

        doExecute();

        this.setCurrentExecutedTimes(this.getCurrentExecutedTimes() + 1);
        if (isFinished()) {
            finishTask();
        } else {
            changeStatus();
        }

        afterDoExecute();

        log.info("结束执行定时任务:{}", getTopic());
    }

    public void beforeDoExecute() {
        // to be extended
    }

    public void afterDoExecute() {
        // to be extended
    }

    /**
     * 任务未结束，修改任务的相关状态
     */
    private void changeStatus() {
        ApplicationContext applicationContext = SpringUtil.getApplicationContext();
        ScheduleTaskService scheduleTaskService = applicationContext.getBean(ScheduleTaskService.class);
        // 当前执行日期(理论上应该大于等于预计执行时间)
        Date executeDate = new Date();
        List<Date> dates = CronPatternUtil.matchedDates(getCronExpression(), executeDate, 1, true);
        // 下一次执行日期
        LocalDateTime nextExecuteTime = LocalDateTimeUtil.of(dates.get(0));

        // 修改当前对象的下次执行日期
        setNextExecutionTime(nextExecuteTime);

        // 修改数据库
        scheduleTaskService
                .lambdaUpdate()
                .eq(ScheduleTask::getId, this.getId())
                .set(ScheduleTask::getLastExecutionTime, executeDate)
                .set(ScheduleTask::getNextExecutionTime, nextExecuteTime)
                .set(ScheduleTask::getCurrentExecutedTimes, getCurrentExecutedTimes())
                .set(ScheduleTask::getUpdateTime, executeDate)
                .update();
    }

    /**
     * 将任务修改为完成状态
     */
    private void finishTask() {
        // 从定时任务中删除任务
        ScheduleTaskHelper.removeTask(this.getId());

        // 修改任务持久化的状态
        ScheduleTaskService scheduleTaskService = SpringUtil
                .getApplicationContext()
                .getBean(ScheduleTaskService.class);

        scheduleTaskService
                .lambdaUpdate()
                .eq(ScheduleTask::getId, this.getId())
                .set(ScheduleTask::getNextExecutionTime, null)
                .set(ScheduleTask::getCurrentExecutedTimes, getCurrentExecutedTimes())
                .set(ScheduleTask::getTaskStatus, STATUS_FINISHED)
                .update();
    }

    /**
     * 任务实际执行的逻辑
     */
    public abstract void doExecute();

    /**
     * 任务完成的判断
     *
     * @return 任务是否已经完成
     */
    public abstract boolean isFinished();

}
