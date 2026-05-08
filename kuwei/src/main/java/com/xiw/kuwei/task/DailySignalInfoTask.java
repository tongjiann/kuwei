package com.xiw.kuwei.task;

import com.diboot.core.util.ContextHolder;
import com.xiw.kuwei.entity.common.ScheduleTask;
import com.xiw.kuwei.service.stock.StockCommonService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DailySignalInfoTask extends AbstractTask {


    public DailySignalInfoTask(ScheduleTask scheduleTask) {
        super();
        BeanUtils.copyProperties(scheduleTask, this);
    }

    @Resource


    @Override
    public void doExecute() {
        ContextHolder.getBean(StockCommonService.class).pushDailySignalInfo(getRelatedAccountId());
    }

    @Override
    public boolean isFinished() {
        return false;
    }

}
