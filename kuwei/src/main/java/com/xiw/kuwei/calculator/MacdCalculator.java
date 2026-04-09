package com.xiw.kuwei.calculator;

import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * MACD 指标计算工具（使用 BigDecimal 保证精度）
 */
public class MacdCalculator {

    private static final int FAST = 12;

    private static final int SLOW = 26;

    private static final int SIGNAL = 9;

    // 常量 2，用于 EMA 乘数计算
    private static final BigDecimal TWO = new BigDecimal("2");

    // 默认精度与舍入模式
    private static final int SCALE = 10;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * 为数据列表计算 MACD 指标
     */
    public static void calculate(List<StockDailyInfoVO> list) {
        if (list == null || list.size() < SLOW) return;

        // 提取收盘价 (BigDecimal)
        BigDecimal[] closePrices = list.stream()
                .map(v -> v.getTodayClosePrice())
                .toArray(BigDecimal[]::new);

        // 计算 EMA12 和 EMA26
        BigDecimal[] ema12 = calculateEMA(closePrices, FAST);
        BigDecimal[] ema26 = calculateEMA(closePrices, SLOW);

        // DIF = EMA12 - EMA26
        BigDecimal[] dif = new BigDecimal[closePrices.length];
        for (int i = 0; i < closePrices.length; i++) {
            dif[i] = ema12[i].subtract(ema26[i]);
        }

        // DEA = EMA(DIF, 9)
        BigDecimal[] dea = calculateEMA(dif, SIGNAL);

        // MACD柱 = (DIF - DEA) * 2
        for (int i = 0; i < list.size(); i++) {
            BigDecimal macdVal = dif[i].subtract(dea[i]).multiply(TWO);
            // 创建 MacdInfo 时保留适当精度（如保留 4 位小数）
            list.get(i).setMacdInfo(new MacdInfo(
                    dif[i].setScale(4, ROUNDING_MODE),
                    dea[i].setScale(4, ROUNDING_MODE),
                    macdVal.setScale(4, ROUNDING_MODE)
            ));
        }
    }

    /**
     * 计算指数移动平均 (EMA) —— BigDecimal 版本
     */
    private static BigDecimal[] calculateEMA(BigDecimal[] data, int period) {
        BigDecimal[] ema = new BigDecimal[data.length];
        if (data.length == 0) return ema;

        // multiplier = 2 / (period + 1)
        BigDecimal multiplier = TWO.divide(new BigDecimal(period + 1), SCALE, ROUNDING_MODE);

        // 首个有效值用 SMA (简单移动平均)
        BigDecimal sum = BigDecimal.ZERO;
        int count = Math.min(period, data.length);
        for (int i = 0; i < count; i++) {
            sum = sum.add(data[i]);
        }
        BigDecimal sma = sum.divide(new BigDecimal(count), SCALE, ROUNDING_MODE);
        ema[period - 1] = sma;

        // 后续用 EMA 公式: EMA(today) = (Price(today) - EMA(yesterday)) * multiplier + EMA(yesterday)
        for (int i = period; i < data.length; i++) {
            BigDecimal diff = data[i].subtract(ema[i - 1]);
            BigDecimal term = diff.multiply(multiplier);
            ema[i] = term.add(ema[i - 1]);
        }

        // 前 period-1 个值填充为第一个有效 EMA 值
        for (int i = 0; i < period - 1; i++) {
            ema[i] = ema[period - 1];
        }

        return ema;
    }
}