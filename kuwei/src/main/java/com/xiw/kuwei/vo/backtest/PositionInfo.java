package com.xiw.kuwei.vo.backtest;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionInfo {

    private String code;

    private BigDecimal position = BigDecimal.ZERO;      // 持仓股数
    private BigDecimal avgCost = BigDecimal.ZERO;       // 平均成本
    private BigDecimal totalCost = BigDecimal.ZERO;     // 总成本

    private BigDecimal price = BigDecimal.ZERO;         // 当日价格
    private BigDecimal positionValue = BigDecimal.ZERO; // 市值
}