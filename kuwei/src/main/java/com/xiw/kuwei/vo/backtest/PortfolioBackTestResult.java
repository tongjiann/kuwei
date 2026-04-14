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

    private BigDecimal returnRate;

    private BigDecimal maxDrawDown;

    private BigDecimal sharpeRatio;

    private String strategy;

    @JsonIgnore
    private List<Signal> signalList;

    @JsonIgnore
    private List<TradeDetail> tradeDetailList;


}