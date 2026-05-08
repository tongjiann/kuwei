package com.xiw.kuwei.helper.fetcher;

import com.xiw.kuwei.annotation.Fetcher;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import com.xiw.kuwei.entity.stock.StockInfo;
import com.xiw.kuwei.vo.stock.SimpleStockVO;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.util.List;
import java.util.Map;

public abstract class AbstractFetcher {

    private String platform = null;

    public abstract List<StockDailyInfo> getStockDailyInfo(StockInfo stockInfo);

    public abstract List<StockDailyInfo> getStockDailyInfo(StockInfo stockInfo, int days);

    public StockInfo getStockInfo(String code, String name) {
        StockInfo stockInfo = doGetStockInfo(code, name);
        if (platform == null) {
            platform = this.getClass().getAnnotation(Fetcher.class).platform();
        }
        stockInfo.setSource(platform);
        return stockInfo;
    }

    abstract StockInfo doGetStockInfo(String code, String name);

    public abstract List<SimpleStockVO> getSimpleStockVO(String key);

    public abstract Map<String, StockDailyInfoVO> getCurrentStockDailyInfo(List<StockInfo> stockInfoList);


}
