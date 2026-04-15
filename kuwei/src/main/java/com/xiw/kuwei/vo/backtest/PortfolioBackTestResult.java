package com.xiw.kuwei.vo.backtest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioBackTestResult {

    private List<PortfolioDailyRecord> portfolioDailyRecordList;

    private BigDecimal startAsset;

    private BigDecimal endAsset;

    /**
     * 收益率
     */
    private BigDecimal returnRate;

    /**
     * 最大回撤
     */
    private BigDecimal maxDrawDown;

    /**
     * 夏普率
     */
    private BigDecimal sharpeRatio;

    private String strategy;

    @JsonIgnore
    private List<Signal> signalList;

    private Integer signalSize;

    @JsonIgnore
    private List<TradeDetail> tradeDetailList;

    private Integer tradeDetailSize;


}