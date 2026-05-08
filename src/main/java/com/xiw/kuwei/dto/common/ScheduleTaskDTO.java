package com.xiw.kuwei.dto.common;

import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.xiw.kuwei.entity.common.ScheduleTask;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
* 定时任务 DTO定义
*
* @author xiw
* @version 1.0
* @date 2026-05-08
 * Copyright © xiwwwwww
*/
@Getter
@Setter
@Accessors(chain = true)
public class ScheduleTaskDTO extends ScheduleTask  {
    private static final long serialVersionUID = 1151799471864939101L;

    /**
    * id集合
    */
    @BindQuery(comparison = Comparison.IN, field = "id")
    private List<String> ids;

    /**
    * 重要程度 起始
    */
    @BindQuery(comparison = Comparison.GE, field = "importanceLevel")
    private Long importanceLevelBegin;

    /**
    * 重要程度 截止
    */
    @BindQuery(comparison = Comparison.LE, field = "importanceLevel")
    private Long importanceLevelEnd;

    /**
    * 下次执行时间
    */
    @BindQuery(comparison = Comparison.GE)
    private LocalDateTime nextExecutionTime;

    /**
    * 下次执行时间 起始
    */
    @BindQuery(comparison = Comparison.GE, field = "nextExecutionTime")
    private LocalDate nextExecutionTimeBegin;

    /**
    * 下次执行时间 截止
    */
    @BindQuery(comparison = Comparison.LT, field = "nextExecutionTime")
    private LocalDate nextExecutionTimeEnd;

    /**
    * 上次执行时间
    */
    @BindQuery(comparison = Comparison.GE)
    private LocalDateTime lastExecutionTime;

    /**
    * 上次执行时间 起始
    */
    @BindQuery(comparison = Comparison.GE, field = "lastExecutionTime")
    private LocalDate lastExecutionTimeBegin;

    /**
    * 上次执行时间 截止
    */
    @BindQuery(comparison = Comparison.LT, field = "lastExecutionTime")
    private LocalDate lastExecutionTimeEnd;

    /**
    * 最大执行次数 起始
    */
    @BindQuery(comparison = Comparison.GE, field = "maxExecutionTimes")
    private Long maxExecutionTimesBegin;

    /**
    * 最大执行次数 截止
    */
    @BindQuery(comparison = Comparison.LE, field = "maxExecutionTimes")
    private Long maxExecutionTimesEnd;

    /**
    * 当前已执行次数 起始
    */
    @BindQuery(comparison = Comparison.GE, field = "currentExecutedTimes")
    private Long currentExecutedTimesBegin;

    /**
    * 当前已执行次数 截止
    */
    @BindQuery(comparison = Comparison.LE, field = "currentExecutedTimes")
    private Long currentExecutedTimesEnd;

    /**
    * 开始执行时间
    */
    @BindQuery(comparison = Comparison.GE)
    private LocalDateTime startExecutionTime;

    /**
    * 开始执行时间 起始
    */
    @BindQuery(comparison = Comparison.GE, field = "startExecutionTime")
    private LocalDate startExecutionTimeBegin;

    /**
    * 开始执行时间 截止
    */
    @BindQuery(comparison = Comparison.LT, field = "startExecutionTime")
    private LocalDate startExecutionTimeEnd;

    /**
    * 结束执行时间
    */
    @BindQuery(comparison = Comparison.GE)
    private LocalDateTime endExecutionTime;

    /**
    * 结束执行时间 起始
    */
    @BindQuery(comparison = Comparison.GE, field = "endExecutionTime")
    private LocalDate endExecutionTimeBegin;

    /**
    * 结束执行时间 截止
    */
    @BindQuery(comparison = Comparison.LT, field = "endExecutionTime")
    private LocalDate endExecutionTimeEnd;

    /**
    * 创建时间
    */
    @BindQuery(comparison = Comparison.GE)
    private LocalDateTime createTime;

    /**
    * 创建时间 起始
    */
    @BindQuery(comparison = Comparison.GE, field = "createTime")
    private LocalDate createTimeBegin;

    /**
    * 创建时间 截止
    */
    @BindQuery(comparison = Comparison.LT, field = "createTime")
    private LocalDate createTimeEnd;

    /**
    * 更新时间
    */
    @BindQuery(comparison = Comparison.GE)
    private LocalDateTime updateTime;

    /**
    * 更新时间 起始
    */
    @BindQuery(comparison = Comparison.GE, field = "updateTime")
    private LocalDate updateTimeBegin;

    /**
    * 更新时间 截止
    */
    @BindQuery(comparison = Comparison.LT, field = "updateTime")
    private LocalDate updateTimeEnd;

    public LocalDate getNextExecutionTimeEnd() {
        if (getNextExecutionTime() != null) {
            return getNextExecutionTime().plusDays(1).toLocalDate();
        }
        if (nextExecutionTimeEnd != null) {
            return nextExecutionTimeEnd.plusDays(1);
        }
        return null;
    }

    public LocalDate getLastExecutionTimeEnd() {
        if (getLastExecutionTime() != null) {
            return getLastExecutionTime().plusDays(1).toLocalDate();
        }
        if (lastExecutionTimeEnd != null) {
            return lastExecutionTimeEnd.plusDays(1);
        }
        return null;
    }

    public LocalDate getStartExecutionTimeEnd() {
        if (getStartExecutionTime() != null) {
            return getStartExecutionTime().plusDays(1).toLocalDate();
        }
        if (startExecutionTimeEnd != null) {
            return startExecutionTimeEnd.plusDays(1);
        }
        return null;
    }

    public LocalDate getEndExecutionTimeEnd() {
        if (getEndExecutionTime() != null) {
            return getEndExecutionTime().plusDays(1).toLocalDate();
        }
        if (endExecutionTimeEnd != null) {
            return endExecutionTimeEnd.plusDays(1);
        }
        return null;
    }

    public LocalDate getCreateTimeEnd() {
        if (getCreateTime() != null) {
            return getCreateTime().plusDays(1).toLocalDate();
        }
        if (createTimeEnd != null) {
            return createTimeEnd.plusDays(1);
        }
        return null;
    }

    public LocalDate getUpdateTimeEnd() {
        if (getUpdateTime() != null) {
            return getUpdateTime().plusDays(1).toLocalDate();
        }
        if (updateTimeEnd != null) {
            return updateTimeEnd.plusDays(1);
        }
        return null;
    }

}