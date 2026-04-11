package com.xiw.kuwei.service.stock;

import com.xiw.kuwei.vo.backtest.PortfolioBackTestResult;
import com.xiw.kuwei.vo.stock.StockInfoVO;

import java.util.List;

public interface StockCommonService {

    String initStockInfo(String code, String name);

    void updateStockInfo();

    void updateStockDailyInfo();

    StockInfoVO getExtraStockInfo(String stockId, int days);

    void createChartByStockInfo(StockInfoVO stockId);

    void createCharts(int days);

    List<PortfolioBackTestResult> multiTest(String code);

    void syncDailyInfo();

}
