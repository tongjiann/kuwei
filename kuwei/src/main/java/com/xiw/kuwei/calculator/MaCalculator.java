package com.xiw.kuwei.calculator;

import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MaCalculator {

    private static final int SCALE = 2;

    public static void calculate(List<StockDailyInfoVO> sortedList) {

        BigDecimal sum5 = BigDecimal.ZERO;
        BigDecimal sum10 = BigDecimal.ZERO;
        BigDecimal sum20 = BigDecimal.ZERO;
        BigDecimal sum30 = BigDecimal.ZERO;

        for (int i = 0; i < sortedList.size(); i++) {

            StockDailyInfoVO item = sortedList.get(i);
            BigDecimal close = item.getTodayClosePrice();

            sum5 = sum5.add(close);
            sum10 = sum10.add(close);
            sum20 = sum20.add(close);
            sum30 = sum30.add(close);

            // 移除窗口外数据
            if (i >= 5) {
                sum5 = sum5.subtract(sortedList.get(i - 5).getTodayClosePrice());
            }
            if (i >= 10) {
                sum10 = sum10.subtract(sortedList.get(i - 10).getTodayClosePrice());
            }
            if (i >= 20) {
                sum20 = sum20.subtract(sortedList.get(i - 20).getTodayClosePrice());
            }
            if (i >= 30) {
                sum30 = sum30.subtract(sortedList.get(i - 30).getTodayClosePrice());
            }

            MaInfo maInfo = new MaInfo();

            // 不足周期的不赋值（保持 null）
            if (i >= 4) {
                maInfo.setMa5(scale(sum5.divide(BigDecimal.valueOf(5), 10, RoundingMode.HALF_UP)));
            }
            if (i >= 9) {
                maInfo.setMa10(scale(sum10.divide(BigDecimal.valueOf(10), 10, RoundingMode.HALF_UP)));
            }
            if (i >= 19) {
                maInfo.setMa20(scale(sum20.divide(BigDecimal.valueOf(20), 10, RoundingMode.HALF_UP)));
            }
            if (i >= 29) {
                maInfo.setMa30(scale(sum30.divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP)));
            }

            item.setMaInfo(maInfo);
        }
    }

    private static BigDecimal scale(BigDecimal val) {
        return val.setScale(SCALE, RoundingMode.HALF_UP);
    }

}