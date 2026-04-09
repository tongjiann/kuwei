package com.xiw.kuwei.calculator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    public MacdInfo(BigDecimal dif, BigDecimal dea, BigDecimal macd) {
        this.dif = dif;
        this.dea = dea;
        this.macd = macd;
    }

}