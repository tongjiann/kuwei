package com.xiw.kuwei.constant;

/**
 * @author xiwang
 * @apiNote
 * @since 2022-10-18 14:16
 */
public final class ScheduleTaskConstant {

    /**
     * 任务类型：消息
     */
    public static final int TASK_TYPE_MESSAGE = 0;

    /**
     * 任务类型：每日股票信号
     */
    public static final int TASK_TYPE_DAILY_STOCK_SIGNAL = 1;


    /**
     * 执行方式：准时
     */
    public static final int EXECUTE_TYPE_ON_TIME = 0;

    /**
     * 执行方式：提前
     */
    public static final int EXECUTE_TYPE_ADVANCE = 1;

    /**
     * 状态：正在执行
     */
    public static final int STATUS_PROCESSING = 0;

    /**
     * 状态：中止
     */
    public static final int STATUS_STOPPED = 1;

    /**
     * 状态：已经结束
     */
    public static final int STATUS_FINISHED = 2;

    private ScheduleTaskConstant() {
    }

}
