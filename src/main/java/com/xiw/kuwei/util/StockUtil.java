package com.xiw.kuwei.util;

import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import com.xiw.kuwei.vo.stock.StockInfoVO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StockUtil {


    /**
     * 模拟年化收益曲线
     */
    public static StockInfoVO simulateAnnualizedReturnData(List<LocalDate> tradingDateList, LocalDate startDate, LocalDate endDate, BigDecimal annualizedReturn) {
        // 创建一个虚拟的 StockInfoVO 用于标识这条线
        StockInfoVO annualBenchmark = new StockInfoVO();
        annualBenchmark.setCode("ANNUAL_" + annualizedReturn);
        annualBenchmark.setName("年化" + annualizedReturn + "%基准");
        List<StockDailyInfoVO> annualLine = new ArrayList<>();
        annualBenchmark.setStockDailyInfoVOList(annualLine);
        if (startDate != null && endDate != null) {
            // 获取基准日期列表（使用 stockInfoVO 中在 [startDate, endDate] 范围内的所有交易日）
            if (!tradingDateList.isEmpty()) {

                // 计算年化收益率对应的每日复利因子（基于交易日数量）
                int tradingDays = tradingDateList.size();
                // 年化15% -> 总收益率 = (1+年化收益)^(年数)
                BigDecimal years = new BigDecimal(endDate.toEpochDay() - startDate.toEpochDay())
                        .divide(new BigDecimal("365.25"), 6, RoundingMode.HALF_EVEN);
                BigDecimal totalReturn = BigDecimalUtil.pow(BigDecimal.ONE.add(annualizedReturn), years);

                // 每天线性增长
                BigDecimal dailyIncrement = totalReturn.subtract(BigDecimal.ONE)
                        .divide(new BigDecimal(tradingDays), 10, RoundingMode.HALF_EVEN);

                BigDecimal nav = BigDecimal.ONE;

                for (LocalDate date : tradingDateList) {
                    StockDailyInfoVO point = new StockDailyInfoVO();
                    point.setDate(date);
                    point.setTodayClosePrice(nav);

                    annualLine.add(point);

                    nav = nav.add(dailyIncrement);
                }
            }
        }
        return annualBenchmark;
    }


}
