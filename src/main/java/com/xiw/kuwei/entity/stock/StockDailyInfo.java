package com.xiw.kuwei.entity.stock;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diboot.core.entity.BaseModel;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

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
