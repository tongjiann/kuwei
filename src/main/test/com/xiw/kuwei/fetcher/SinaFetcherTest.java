package com.xiw.kuwei.fetcher;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import com.xiw.kuwei.entity.stock.StockInfo;
import com.xiw.kuwei.helper.fetcher.SinaFetcher;
import org.junit.jupiter.api.Test;

import java.util.List;

class SinaFetcherTest {

    @Test
    void testGetStockInfo() {
        SinaFetcher fetcher = new SinaFetcher();
        StockInfo stockInfo = fetcher.getStockInfo("code001", "name001");
        assert stockInfo != null;
        assert stockInfo.getId() == null;
        assert stockInfo.getName() != null;
        assert stockInfo.getCode() != null;
        System.out.println(stockInfo);
    }

    @Test
    void testGetStockDailyInfo() {
        SinaFetcher fetcher = new SinaFetcher();
        StockInfo stockInfo = fetcher.getStockInfo("sh603459", "name001");
        stockInfo.setId(IdWorker.getIdStr());
        List<StockDailyInfo> stockDailyInfoList = fetcher.getStockDailyInfo(stockInfo);
        System.out.println(stockDailyInfoList);
    }

    @Test
    void testGetStockPlateDailyInfo() {
        SinaFetcher fetcher = new SinaFetcher();
        StockInfo stockInfo = fetcher.getStockInfo("si930901", "name001");
        stockInfo.setId(IdWorker.getIdStr());
        List<StockDailyInfo> stockDailyInfoList = fetcher.getStockDailyInfo(stockInfo);
        System.out.println(stockDailyInfoList);
    }

}
