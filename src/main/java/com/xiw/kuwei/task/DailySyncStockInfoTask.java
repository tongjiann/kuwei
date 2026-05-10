package com.xiw.kuwei.task;

import com.diboot.core.util.ContextHolder;
import com.xiw.kuwei.entity.common.ScheduleTask;
import com.xiw.kuwei.service.stock.StockCommonService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DailySyncStockInfoTask extends AbstractTask {


    public DailySyncStockInfoTask(ScheduleTask scheduleTask) {
        super();
        BeanUtils.copyProperties(scheduleTask, this);
    }


    @Override
    public void doExecute() {
        ContextHolder.getBean(StockCommonService.class).syncDailyInfo(null);
    }

    @Override
    public boolean isFinished() {
        return false;
    }

}
