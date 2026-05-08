package com.xiw.kuwei.entity.common;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.diboot.core.entity.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * 定时任务 Entity 定义
 * @author xiw
 * @version 1.0
 * @date 2026-05-08
 * Copyright © xiwwwwww
 */
@TableName("mdl_schedule_task")
@Getter
@Setter
@Accessors(chain = true)
public class ScheduleTask extends BaseModel {

    private static final long serialVersionUID = 1601221896272074785L;

    /**
     * 主题
     */
    @Length(max = 100, message = "主题长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String topic;

    /**
     * 详情
     */
    @Length(max = 100, message = "详情长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String detail;

    /**
     * 重要程度
     */
    @TableField()
    private Long importanceLevel;

    /**
     * CRON表达式
     */
    @Length(max = 100, message = "CRON表达式长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String cronExpression;

    /**
     * 任务类型
     */
    @TableField()
    private String taskType;

    /**
     * 执行类型
     */
    @TableField()
    private String executionType;

    /**
     * 下次执行时间
     */
    @TableField()
    private LocalDateTime nextExecutionTime;

    /**
     * 上次执行时间
     */
    @TableField()
    private LocalDateTime lastExecutionTime;

    /**
     * 最大执行次数
     */
    @TableField()
    private Long maxExecutionTimes;

    /**
     * 当前已执行次数
     */
    @TableField()
    private Long currentExecutedTimes;

    /**
     * 开始执行时间
     */
    @TableField()
    private LocalDateTime startExecutionTime;

    /**
     * 结束执行时间
     */
    @TableField()
    private LocalDateTime endExecutionTime;

    /**
     * 任务状态
     */
    @TableField()
    private String taskStatus;

    /**
     * 创建者
     */
    @TableField()
    private String relatedAccountId;
}
