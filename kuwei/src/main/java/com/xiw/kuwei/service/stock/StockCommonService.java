package com.xiw.kuwei.service.stock;

import com.xiw.kuwei.vo.backtest.PortfolioBackTestResult;
import com.xiw.kuwei.vo.stock.SimpleStockVO;
import com.xiw.kuwei.vo.stock.StockInfoVO;

import java.time.LocalDate;
import java.util.List;

public interface StockCommonService {

    void initStockInfo(String code, String name);

    void updateStockInfo();

    void updateStockDailyInfo();

    StockInfoVO getExtraStockInfo(String stockId, int days);

    void createChartByStockInfo(StockInfoVO stockId);

    void createCharts(int days);

    List<PortfolioBackTestResult> multiTest(String code, LocalDate startDate);

    void syncDailyInfo(String stockId);

    List<PortfolioBackTestResult> multiTestByCodeList(List<String> codeList, LocalDate startDate);


    List<SimpleStockVO> getSimpleStockInfo(String key);

}
