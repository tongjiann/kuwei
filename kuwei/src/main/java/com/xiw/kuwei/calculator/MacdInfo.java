package com.xiw.kuwei.calculator;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MacdInfo {

    /**
     * EMA12
     */
    private BigDecimal ema12;

    /**
     * EMA26
     */
    private BigDecimal ema26;

    /**
     * 快线 DIF
     */
    private BigDecimal dif;

    /**
     * 慢线 DEA
     */
    private BigDecimal dea;

    /**
     * MACD柱
     */
    private BigDecimal macd;

}