package com.xiw.kuwei.helper.fetcher;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiw.kuwei.annotation.Fetcher;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import com.xiw.kuwei.entity.stock.StockInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Fetcher(type = 1, order = 1, platform = "新浪财经")
public class SinaFetcher extends abstractFetcher {

    private static final int DEFAULT_DAY_LENGTH = 365 * 5;

    private static final int DEFAULT_TRANSACTION_MINUTE = 240;

    private static final String API_HISTORY_INFO_TEMPLATE = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol={}&scale=240&ma=no&datalen={}";

    private static final String API_HISTORY_PLATE_INFO_TEMPLATE = "https://quotes.sina.cn/cn/api/jsonp_v2.php/var%20_{}_{}_{}=/CN_MarketDataService.getKLineData?symbol={}&scale={}&ma=no&datalen={}";

    private static final Logger log = LoggerFactory.getLogger(SinaFetcher.class);

    private static List<StockDailyInfo> parsedArrayFromJsom(int size, JSONArray jsonArray, String stockId) {
        List<StockDailyInfo> stockDailyInfos = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Date day = jsonObject.getDate("day");
            BigDecimal open = jsonObject.getBigDecimal("open");
            BigDecimal high = jsonObject.getBigDecimal("high");
            BigDecimal low = jsonObject.getBigDecimal("low");
            BigDecimal close = jsonObject.getBigDecimal("close");
            BigDecimal volume = jsonObject.getBigDecimal("volume");
            StockDailyInfo stockDailyInfo = StockDailyInfo
                    .builder()
                    .stockId(stockId)
                    .date(LocalDateTimeUtil.of(day).toLocalDate())
                    .openPrice(open)
                    .highPrice(high)
                    .lowPrice(low)
                    .todayClosePrice(close)
                    .turnover(volume).build();
            stockDailyInfos.add(stockDailyInfo);
        }
        return stockDailyInfos;
    }

    @Override
    public List<StockDailyInfo> getStockDailyInfo(StockInfo stockInfo) {
        List<StockDailyInfo> stockDailyInfo;
        log.info("尝试获取股票数据");
        stockDailyInfo = doGetStockDailyInfo(stockInfo);
        if (!CollUtil.isEmpty(stockDailyInfo)) {
            return stockDailyInfo;
        }
        log.info("尝试获取板块数据");
        stockDailyInfo = getPlateDailyInfo(stockInfo);
        if (!CollUtil.isEmpty(stockDailyInfo)) {
            return stockDailyInfo;
        }
        log.info("未获取到获取日线数据");
        return stockDailyInfo;
    }

    private List<StockDailyInfo> doGetStockDailyInfo(StockInfo stockInfo) {
        if (stockInfo == null) {
            throw new NullPointerException("stockInfo is null");
        }
        String code = stockInfo.getCode();
        if (code == null) {
            throw new NullPointerException("code is null");
        }
        String stockId = stockInfo.getId();
        String formattedUrl = CharSequenceUtil.format(API_HISTORY_INFO_TEMPLATE, code, DEFAULT_DAY_LENGTH);
        System.out.println(formattedUrl);
        String response = HttpUtil.get(formattedUrl);
        if (StrUtil.isBlank(response) || "null".equals(response)) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSONUtil.parseArray(response);
        int size = jsonArray.size();
        if (size == 0) {
            return Collections.emptyList();
        }
        List<StockDailyInfo> stockDailyInfos = parsedArrayFromJsom(size, jsonArray, stockId);
        return stockDailyInfos;
    }

    private List<StockDailyInfo> getPlateDailyInfo(StockInfo stockInfo) {
        if (stockInfo == null) {
            throw new NullPointerException("stockInfo is null");
        }
        String code = stockInfo.getCode();
        if (code == null) {
            throw new NullPointerException("code is null");
        }
        String stockId = stockInfo.getId();
        String formattedUrl = CharSequenceUtil.format(API_HISTORY_PLATE_INFO_TEMPLATE, code, DEFAULT_TRANSACTION_MINUTE, System.currentTimeMillis(), code, DEFAULT_TRANSACTION_MINUTE, DEFAULT_DAY_LENGTH);
        String response = HttpUtil.get(formattedUrl);
        JSONArray jsonArray = new JSONArray();
        try {
            int start = response.indexOf('[');
            int end = response.lastIndexOf(']');
            if (start != -1 || end != -1) {
                jsonArray = JSONUtil.parseArray(response.substring(start, end + 1));
            }
        } catch (Exception e) {
            log.info("解析异常，原文：{}", response);
        }
        int size = jsonArray.size();
        if (size == 0) {
            return Collections.emptyList();
        }
        List<StockDailyInfo> stockDailyInfos = parsedArrayFromJsom(size, jsonArray, stockId);
        return stockDailyInfos;

    }

    @Override
    public StockInfo doGetStockInfo(String code, String name) {
        return StockInfo.builder().code(code).name(name).build();
    }

}
