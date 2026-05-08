package com.xiw.kuwei.calculator;

import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/**
 * MACD 计算器（支持自定义参数）
 */
public class MacdCalculator {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_EVEN);

    private final int fastPeriod;

    private final int slowPeriod;

    private final int signalPeriod;

    public MacdCalculator(int fastPeriod, int slowPeriod, int signalPeriod) {
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
    }

    public void calculate(List<StockDailyInfoVO> list) {

        if (list == null || list.isEmpty()) return;

        BigDecimal[] emaFast = new BigDecimal[list.size()];
        BigDecimal[] emaSlow = new BigDecimal[list.size()];
        BigDecimal[] difArr = new BigDecimal[list.size()];
        BigDecimal[] deaArr = new BigDecimal[list.size()];

        BigDecimal fastK = BigDecimal.valueOf(2.0 / (fastPeriod + 1));
        BigDecimal slowK = BigDecimal.valueOf(2.0 / (slowPeriod + 1));
        BigDecimal signalK = BigDecimal.valueOf(2.0 / (signalPeriod + 1));

        for (int i = 0; i < list.size(); i++) {

            BigDecimal close = list.get(i).getTodayClosePrice();

            if (i == 0) {
                emaFast[i] = close;
                emaSlow[i] = close;
                difArr[i] = BigDecimal.ZERO;
                deaArr[i] = BigDecimal.ZERO;
            } else {
                emaFast[i] = close.multiply(fastK, MC)
                        .add(emaFast[i - 1].multiply(BigDecimal.ONE.subtract(fastK), MC), MC);

                emaSlow[i] = close.multiply(slowK, MC)
                        .add(emaSlow[i - 1].multiply(BigDecimal.ONE.subtract(slowK), MC), MC);

                difArr[i] = emaFast[i].subtract(emaSlow[i], MC);

                deaArr[i] = difArr[i].multiply(signalK, MC)
                        .add(deaArr[i - 1].multiply(BigDecimal.ONE.subtract(signalK), MC), MC);
            }

            BigDecimal macd = difArr[i].subtract(deaArr[i], MC).multiply(BigDecimal.valueOf(2));

            MacdInfo macdInfo = new MacdInfo();
            macdInfo.setDif(difArr[i]);
            macdInfo.setDea(deaArr[i]);
            macdInfo.setMacd(macd);

            list.get(i).setMacdInfo(macdInfo);
        }
    }
}