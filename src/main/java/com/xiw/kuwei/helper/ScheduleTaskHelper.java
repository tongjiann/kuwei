package com.xiw.kuwei.helper;

import cn.hutool.cron.CronUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.xiw.kuwei.config.ScheduleTaskConfig;
import com.xiw.kuwei.entity.common.ScheduleTask;
import com.xiw.kuwei.factory.ScheduleTaskFactory;
import com.xiw.kuwei.service.common.ScheduleTaskService;
import com.xiw.kuwei.task.AbstractTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.xiw.kuwei.constant.ScheduleTaskConstant.STATUS_PROCESSING;


/**
 * @author xiwang
 * @apiNote
 * @since 2022-10-18 11:02
 */
@Slf4j
@Component
public class ScheduleTaskHelper {

    private static final Map<String, ScheduleTask> scheduleTaskMap = new HashMap<>();

    private static volatile boolean onRefresh = false;

    static {
        CronUtil.setMatchSecond(true);
        if (ScheduleTaskConfig.isEnable()) {
            refresh();
        }
    }

    public static void refresh() {
        if (!onRefresh) {
            synchronized (ScheduleTask.class) {
                if (!onRefresh) {
                    onRefresh = true;

                    doRefresh();

                    onRefresh = false;

                }
            }
        }
    }

    private static void doRefresh() {
        log.info("刷新定时任务");
        stopAndClean();

        List<ScheduleTask> dbScheduleTaskList = getScheduleTaskList();

        createTaskList(dbScheduleTaskList);

        start();
    }

    private static void start() {
        log.info("启动定时任务");
        CronUtil.start(true);
    }

    private static void createTaskList(List<ScheduleTask> dbScheduleTaskList) {
        if (CollectionUtils.isEmpty(dbScheduleTaskList)) {
            return;
        }
        dbScheduleTaskList.forEach(ScheduleTaskHelper::addTask);
    }

    private static List<ScheduleTask> getScheduleTaskList() {
        ApplicationContext applicationContext = SpringUtil.getApplicationContext();
        ScheduleTaskService scheduleTaskService = applicationContext.getBean(ScheduleTaskService.class);
        return scheduleTaskService
                .lambdaQuery()
                .eq(ScheduleTask::getTaskStatus, STATUS_PROCESSING)
                .list();
    }

    private static void stopAndClean() {
        if (CronUtil
                .getScheduler()
                .isStarted()) {
            log.info("关闭定时任务");
            CronUtil.stop();
        }
        log.info("清理所有定时任务");

        Set<String> idList = scheduleTaskMap.keySet();

        idList.forEach(CronUtil::remove);

        scheduleTaskMap.clear();
    }

    public static boolean addTask(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        ScheduleTaskService scheduleTaskService = SpringUtil
                .getApplicationContext()
                .getBean(ScheduleTaskService.class);
        ScheduleTask scheduleTask = scheduleTaskService
                .lambdaQuery()
                .eq(ScheduleTask::getId, id)
                .one();
        if (scheduleTask == null) {
            return false;
        }
        return addTask(scheduleTask);
    }

    public static boolean addTask(ScheduleTask scheduleTask) {
        if (scheduleTask == null) {
            return false;
        }
        AbstractTask abstractTask = ScheduleTaskFactory.getAbstractTask(scheduleTask);
        CronUtil.schedule(scheduleTask.getId(), scheduleTask.getCronExpression(), abstractTask);
        scheduleTaskMap.put(abstractTask.getId(), abstractTask);
        return true;
    }

    public static boolean removeTask(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        if (isPresent(id)) {
            scheduleTaskMap.remove(id);
            return CronUtil.remove(id);
        }
        return false;
    }

    private static boolean isPresent(String id) {
        return scheduleTaskMap.containsKey(id);
    }

}
