package com.xiw.kuwei.calculator;

import com.diboot.core.util.BeanUtils;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MacdCalculator {

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private static final int SCALE = 2; // 保留2位小数

    /**
     * @param list sorted by Date asc
     */
    public static void calculate(List<StockDailyInfoVO> sortedList) {

        BigDecimal ema12 = null;
        BigDecimal ema26 = null;
        BigDecimal dea = BigDecimal.ZERO;

        BigDecimal alpha12 = TWO.divide(BigDecimal.valueOf(13), 10, RoundingMode.HALF_UP);
        BigDecimal alpha26 = TWO.divide(BigDecimal.valueOf(27), 10, RoundingMode.HALF_UP);
        BigDecimal alpha9 = TWO.divide(BigDecimal.valueOf(10), 10, RoundingMode.HALF_UP);


        for (StockDailyInfoVO item : sortedList) {

            BigDecimal close = item.getTodayClosePrice();

            if (ema12 == null) {
                ema12 = close;
                ema26 = close;
            } else {
                ema12 = close.multiply(alpha12)
                        .add(ema12.multiply(BigDecimal.ONE.subtract(alpha12)));

                ema26 = close.multiply(alpha26)
                        .add(ema26.multiply(BigDecimal.ONE.subtract(alpha26)));
            }

            BigDecimal dif = ema12.subtract(ema26);

            dea = dif.multiply(alpha9)
                    .add(dea.multiply(BigDecimal.ONE.subtract(alpha9)));

            BigDecimal macd = dif.subtract(dea).multiply(TWO);

            // 保留2位小数
            ema12 = ema12.setScale(SCALE, RoundingMode.HALF_UP);
            ema26 = ema26.setScale(SCALE, RoundingMode.HALF_UP);
            dif = dif.setScale(SCALE, RoundingMode.HALF_UP);
            dea = dea.setScale(SCALE, RoundingMode.HALF_UP);
            macd = macd.setScale(SCALE, RoundingMode.HALF_UP);

            MacdInfo macdInfo = new MacdInfo();
            macdInfo.setEma12(ema12);
            macdInfo.setEma26(ema26);
            macdInfo.setDif(dif);
            macdInfo.setDea(dea);
            macdInfo.setMacd(macd);

            item.setMacdInfo(macdInfo);
        }

    }

}