package com.xiw.kuwei.dto.detector;

import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.xiw.kuwei.entity.detector.DetectorTemplateParameter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 识别器模板参数 DTO定义
 *
 * @author xiw
 * @version 1.0
 * @date 2026-04-15
 * Copyright © xiwwwwww
 */
@Getter
@Setter
@Accessors(chain = true)
public class DetectorTemplateParameterDTO extends DetectorTemplateParameter {

    private static final long serialVersionUID = 1762333895153660799L;

    /**
     * id集合
     */
    @BindQuery(comparison = Comparison.IN, field = "id")
    private List<String> ids;

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

    /**
     * 类型 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "type")
    private Long typeBegin;

    /**
     * 类型 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "type")
    private Long typeEnd;

    /**
     * 默认值 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "defaultValue")
    private BigDecimal defaultValueBegin;

    /**
     * 默认值 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "defaultValue")
    private BigDecimal defaultValueEnd;

    /**
     * 序号 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "index")
    private Long indexBegin;

    /**
     * 序号 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "index")
    private Long indexEnd;
}
