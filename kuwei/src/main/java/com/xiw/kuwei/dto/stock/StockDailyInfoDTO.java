package com.xiw.kuwei.dto.stock;

import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.diboot.iam.entity.IamOrg;
import com.diboot.iam.entity.IamUser;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.xiw.kuwei.entity.stock.StockInfo;

/**
 * 股票每日信息 DTO定义
 *
 * @author xiw
 * @version 1.0
 * @date 2026-04-07
 * Copyright © xiwwwwww
 */
@Getter
@Setter
@Accessors(chain = true)
public class StockDailyInfoDTO extends StockDailyInfo {

    private static final long serialVersionUID = 1542557510516202445L;

    /**
     * id集合
     */
    @BindQuery(comparison = Comparison.IN, field = "id")
    private List<String> ids;

    /**
     * 日期 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "date")
    private LocalDate dateBegin;

    /**
     * 日期 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "date")
    private LocalDate dateEnd;

    /**
     * 昨日收盘价 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "yesterdayClosePrice")
    private BigDecimal yesterdayClosePriceBegin;

    /**
     * 昨日收盘价 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "yesterdayClosePrice")
    private BigDecimal yesterdayClosePriceEnd;

    /**
     * 开盘价 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "openPrice")
    private BigDecimal openPriceBegin;

    /**
     * 开盘价 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "openPrice")
    private BigDecimal openPriceEnd;

    /**
     * 今日收盘价 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "todayClosePrice")
    private BigDecimal todayClosePriceBegin;

    /**
     * 今日收盘价 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "todayClosePrice")
    private BigDecimal todayClosePriceEnd;

    /**
     * 最高价 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "highPrice")
    private BigDecimal highPriceBegin;

    /**
     * 最高价 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "highPrice")
    private BigDecimal highPriceEnd;

    /**
     * 最低价 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "lowPrice")
    private BigDecimal lowPriceBegin;

    /**
     * 最低价 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "lowPrice")
    private BigDecimal lowPriceEnd;

    /**
     * 成交额 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "tradingVolume")
    private BigDecimal tradingVolumeBegin;

    /**
     * 成交额 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "tradingVolume")
    private BigDecimal tradingVolumeEnd;

    /**
     * 成交量 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "turnover")
    private BigDecimal turnoverBegin;

    /**
     * 成交量 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "turnover")
    private BigDecimal turnoverEnd;

    /**
     * 涨跌额 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "changePrice")
    private BigDecimal changePriceBegin;

    /**
     * 涨跌额 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "changePrice")
    private BigDecimal changePriceEnd;

    /**
     * 涨跌幅 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "changePercentage")
    private BigDecimal changePercentageBegin;

    /**
     * 涨跌幅 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "changePercentage")
    private BigDecimal changePercentageEnd;

    /**
     * 换手率 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "turnoverRate")
    private BigDecimal turnoverRateBegin;

    /**
     * 换手率 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "turnoverRate")
    private BigDecimal turnoverRateEnd;

    /**
     * 量比 起始
     */
    @BindQuery(comparison = Comparison.GE, field = "volumeRatio")
    private BigDecimal volumeRatioBegin;

    /**
     * 量比 截止
     */
    @BindQuery(comparison = Comparison.LE, field = "volumeRatio")
    private BigDecimal volumeRatioEnd;

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
