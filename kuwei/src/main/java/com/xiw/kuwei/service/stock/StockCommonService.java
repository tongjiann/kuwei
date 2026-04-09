package com.xiw.kuwei.service.stock;

import com.xiw.kuwei.vo.stock.StockInfoVO;

public interface StockCommonService {

    String initStockInfo(String code, String name);

    void updateStockDailyInfo();


    StockInfoVO getExtraStockInfo(String stockId, int days);

    void createChartByStockInfo(StockInfoVO stockId);

    void createCharts(int days);

    void macdDivergence(String stockId);

    void macdSignal(String stockId);

    void macdSignalByCode(String code);

}
