package com.xiw.kuwei.vo.backtest;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class PortfolioDailyRecord {

    private LocalDate date;

    private BigDecimal cash;

    private BigDecimal totalAsset;

    private BigDecimal totalPositionValue;

    private Map<String, PositionInfo> positionMap;

    private List<TradeDetail> tradeList;

}