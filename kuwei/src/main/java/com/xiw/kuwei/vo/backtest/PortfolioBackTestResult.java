package com.xiw.kuwei.vo.backtest;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioBackTestResult {

    private List<PortfolioDailyRecord> portfolioDailyRecordList;

    private BigDecimal startAsset;

    private BigDecimal endAsset;

    private BigDecimal returnRate;

    private BigDecimal maxDrawdown;

    private BigDecimal sharpeRatio;

    private String strategy;

    private List<Signal> signalList;

    private List<TradeDetail> tradeDetailList;


}