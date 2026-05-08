package com.xiw.kuwei.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.xiw.kuwei.util.chart.BackTestChart;
import com.xiw.kuwei.util.chart.ThsTradeIndexChart;
import com.xiw.kuwei.vo.backtest.PortfolioBackTestResult;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import com.xiw.kuwei.vo.stock.StockInfoVO;
import org.jfree.chart.JFreeChart;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ChartUtil {


    public static JFreeChart createStrategyChart(
            String title,
            Map<String, List<StockDailyInfoVO>> benchmarkMap,
            List<PortfolioBackTestResult> portfolioBackTestResultList) {


        return BackTestChart.createMultiStrategyComparisonChart(
                portfolioBackTestResultList,
                benchmarkMap,
                title
        );
    }


    public static JFreeChart createChartByStockInfo(StockInfoVO stockInfoVO) {
        List<StockDailyInfoVO> stockDailyInfoVOList = stockInfoVO.getStockDailyInfoVOList();
        int size = stockDailyInfoVOList.size();
        String title = CharSequenceUtil.format("{}-{}-{}d-{}", stockInfoVO.getCode(), stockInfoVO.getName(), size, LocalDate.now());
        return ThsTradeIndexChart.createChart(stockInfoVO, title);

    }

}
