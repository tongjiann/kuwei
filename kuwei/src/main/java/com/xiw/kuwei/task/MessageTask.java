package com.xiw.kuwei.task;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.cron.pattern.CronPatternUtil;
import com.xiw.kuwei.entity.common.ScheduleTask;
import com.xiw.kuwei.factory.MessageFactory;
import com.xiw.kuwei.helper.message.MessageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @author xiwang
 * @apiNote
 * @since 2022-10-18 11:18
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MessageTask extends AbstractTask {

    private static final MessageHelper messageHelper = MessageFactory.getMessageHelper();

    public MessageTask(ScheduleTask scheduleTask) {
        super();
        BeanUtils.copyProperties(scheduleTask, this);
    }

    @Override
    public void doExecute() {
        messageHelper.sendMessage(this);
    }

    @Override
    public boolean isFinished() {
        // 设置的结束执行时间
        LocalDateTime setStopExecuteTime = getStartExecutionTime();
        LocalDateTime nextExecuteTime = LocalDateTimeUtil.of(CronPatternUtil
                .matchedDates(getCronExpression(), new Date(), 1, true)
                .get(0));
        // 预定结束时间在下一次执行任务之前，则结束任务
        if (setStopExecuteTime != null && setStopExecuteTime.isBefore(nextExecuteTime)) {
            return true;
        }
        Long maxExecuteTimes = getMaxExecutionTimes();

        LocalDateTime startExecuteTime = this.getStartExecutionTime();
        if (startExecuteTime == null) {
            startExecuteTime = getCreateTime();
        }
        if (maxExecuteTimes != null) {
            List<Date> dates1 = CronPatternUtil.matchedDates(
                    getCronExpression(),
                    Date.from(startExecuteTime.atZone(ZoneId.systemDefault()).toInstant()),
                    maxExecuteTimes.intValue(),
                    true
            );
            LocalDateTime calcStopExecuteTime = LocalDateTimeUtil.of(dates1.get(dates1.size() - 1));
            if (calcStopExecuteTime.isBefore(nextExecuteTime)) {
                return true;
            }
        }

        // 当前执行次数超过最大执行次数，结束任务
        return maxExecuteTimes != null && this.getCurrentExecutedTimes() >= maxExecuteTimes;
    }

}
