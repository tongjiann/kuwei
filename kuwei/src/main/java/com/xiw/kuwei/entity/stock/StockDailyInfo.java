package com.xiw.kuwei.entity.stock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.lang.Double;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import com.diboot.core.util.D;
import com.diboot.core.entity.BaseModel;
import com.diboot.core.entity.BaseEntity;
import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.experimental.Accessors;

/**
 * 股票每日信息 Entity 定义
 *
 * @author xiw
 * @version 1.0
 * @date 2026-04-07
 * Copyright © xiwwwwww
 */
@TableName("mdl_stock_daily_info")
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockDailyInfo extends BaseModel {

    private static final long serialVersionUID = 1737022229485703397L;

    /**
     * 股票id
     */
    @TableField()
    private String stockId;

    /**
     * 日期
     */
    @TableField(value = "`date`")
    private LocalDate date;

    /**
     * 时间
     */
    @TableField(value = "`time`")
    private String time;

    /**
     * 昨日收盘价
     */
    @TableField()
    private BigDecimal yesterdayClosePrice;

    /**
     * 开盘价
     */
    @TableField()
    private BigDecimal openPrice;

    /**
     * 今日收盘价
     */
    @TableField()
    private BigDecimal todayClosePrice;

    /**
     * 最高价
     */
    @TableField()
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    @TableField()
    private BigDecimal lowPrice;

    /**
     * 成交额
     */
    @TableField()
    private BigDecimal tradingVolume;

    /**
     * 成交量
     */
    @TableField()
    private BigDecimal turnover;

    /**
     * 涨跌额
     */
    @TableField()
    private BigDecimal changePrice;

    /**
     * 涨跌幅
     */
    @TableField()
    private BigDecimal changePercentage;

    /**
     * 换手率
     */
    @TableField()
    private BigDecimal turnoverRate;

    /**
     * 量比
     */
    @TableField()
    private BigDecimal volumeRatio;
}
