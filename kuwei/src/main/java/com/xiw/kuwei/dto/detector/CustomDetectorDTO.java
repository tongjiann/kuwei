package com.xiw.kuwei.dto.detector;

import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.xiw.kuwei.entity.detector.CustomDetector;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
* 自定义识别器 DTO定义
*
* @author xiw
* @version 1.0
* @date 2026-04-15
 * Copyright © xiwwwwww
*/
@Getter
@Setter
@Accessors(chain = true)
public class CustomDetectorDTO extends CustomDetector  {
    private static final long serialVersionUID = 1911818573770576520L;

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

}