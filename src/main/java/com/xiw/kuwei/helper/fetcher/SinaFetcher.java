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
import com.xiw.kuwei.constant.SinaStockTypeEnum;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import com.xiw.kuwei.entity.stock.StockInfo;
import com.xiw.kuwei.exception.LogicalException;
import com.xiw.kuwei.vo.stock.SimpleStockVO;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Fetcher(type = 1, order = 1, platform = "新浪财经")
public class SinaFetcher extends AbstractFetcher {

    private static final int DEFAULT_DAY_LENGTH = 365 * 5;

    private static final int DEFAULT_TRANSACTION_MINUTE = 240;

    private static final String API_HISTORY_INFO_TEMPLATE = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol={}&scale=240&ma=no&datalen={}";

    private static final String API_HISTORY_PLATE_INFO_TEMPLATE = "https://quotes.sina.cn/cn/api/jsonp_v2.php/var%20_{}_{}_{}=/CN_MarketDataService.getKLineData?symbol={}&scale={}&ma=no&datalen={}";

    private static final String API_SIMPLE_STOCK_INFO_TEMPLATE = "https://suggest3.sinajs.cn/suggest/?type=11,12,31,32,33,41,71,73,81,85,86,88,100,102,103,114,120,203,204&key={}";

    private static final String API_CURRENT_STOCK_DAILY_INFO_TEMPLATE = "https://hq.sinajs.cn/list={}";

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
        return getStockDailyInfo(stockInfo, DEFAULT_DAY_LENGTH);
    }

    @Override
    public List<StockDailyInfo> getStockDailyInfo(StockInfo stockInfo, int days) {
        List<StockDailyInfo> stockDailyInfo;
        log.info("尝试获取股票数据");
        stockDailyInfo = doGetStockDailyInfo(stockInfo, days);
        if (!CollUtil.isEmpty(stockDailyInfo)) {
            return stockDailyInfo;
        }
        log.info("尝试获取板块数据");
        stockDailyInfo = getPlateDailyInfo(stockInfo, days);
        if (!CollUtil.isEmpty(stockDailyInfo)) {
            return stockDailyInfo;
        }
        log.info("未获取到获取日线数据");
        return stockDailyInfo;
    }

    private List<StockDailyInfo> doGetStockDailyInfo(StockInfo stockInfo, int days) {
        if (stockInfo == null) {
            throw new NullPointerException("stockInfo is null");
        }
        String code = stockInfo.getCode();
        if (code == null) {
            throw new NullPointerException("code is null");
        }
        String stockId = stockInfo.getId();
        String formattedUrl = CharSequenceUtil.format(API_HISTORY_INFO_TEMPLATE, code, days);
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

    /**
     * 解析新浪股票数据
     */
    public static Map<String, StockDailyInfoVO> parsePriceMap(String response) {

        LocalDate now = LocalDate.now();
        Map<String, StockDailyInfoVO> result = new HashMap<>();

        // 匹配：
        // var hq_str_sh600000="...";
        Pattern pattern = Pattern.compile(
                "var\\s+hq_str_(\\w+)\\s*=\\s*\"([^\"]*)\"");

        Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {

            // 股票编码
            String code = matcher.group(1);

            // 逗号数据
            String data = matcher.group(2);

            String[] arr = data.split(",");

            if (arr.length > 3) {
                // 当前价格（第4个字段）
                String currentPrice = arr[3];
                StockDailyInfoVO stockDailyInfoVO = new StockDailyInfoVO();
                stockDailyInfoVO.setTodayClosePrice(new BigDecimal(currentPrice));
                stockDailyInfoVO.setDate(now);
                result.put(code, stockDailyInfoVO);
            }
        }

        return result;
    }

    @Override
    public StockInfo doGetStockInfo(String code, String name) {
        return StockInfo.builder().code(code).name(name).build();
    }

    @Override
    public List<SimpleStockVO> getSimpleStockVO(String key) {
        if (key == null) {
            throw new LogicalException("请输入关键字");
        }
        List<SimpleStockVO> result = new ArrayList<>();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("referer", "https://finance.sina.cn/");
        headerMap.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36");
        String response = HttpUtil.createGet(CharSequenceUtil.format(API_SIMPLE_STOCK_INFO_TEMPLATE, key))
                .headerMap(headerMap, true)
                .execute()
                .body();
        Pattern pattern = Pattern.compile("var suggestvalue=\"(.*?)\";");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String data = matcher.group(1);
            if ("".equals(data)) {
                return Collections.emptyList();
            }
            // 按分号拆分
            for (String entry : data.split(";")) {
                String[] parts = entry.split(",");

                // 例如打印第一个字段（股票代码）
                int type = Integer.parseInt(parts[1]);
                result.add(new SimpleStockVO(parts[4], parts[3], type, SinaStockTypeEnum.getDescByType(type)));
            }
        }
        return result;
    }

    public static void main(String[] args) {

        String response = """
                var hq_str_sh600000 = "浦发银行,9.080,9.090,9.070,9.130,9.050,9.060,9.070,89269033,811024242.000";
                var hq_str_sz000001 = "平安银行,11.340,11.370,11.300,11.420,11.300,11.300,11.310";
                var hq_str_sh601398 = "工商银行,7.410,7.390,7.460,7.490,7.410,7.450,7.460";
                """;


        System.out.println(parsePriceMap(response));
    }

    private List<StockDailyInfo> getPlateDailyInfo(StockInfo stockInfo, int days) {
        if (stockInfo == null) {
            throw new NullPointerException("stockInfo is null");
        }
        String code = stockInfo.getCode();
        if (code == null) {
            throw new NullPointerException("code is null");
        }
        String stockId = stockInfo.getId();
        String formattedUrl = CharSequenceUtil.format(API_HISTORY_PLATE_INFO_TEMPLATE, code, days, System.currentTimeMillis(), code, DEFAULT_TRANSACTION_MINUTE, days * 2);
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
    public Map<String, StockDailyInfoVO> getCurrentStockDailyInfo(List<StockInfo> stockInfoList) {
        String formattedUrl = CharSequenceUtil.format(API_CURRENT_STOCK_DAILY_INFO_TEMPLATE, stockInfoList.stream()
                .map(StockInfo::getCode)
                .collect(Collectors.joining(",")));
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("referer", "https://finance.sina.cn/");
        headerMap.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36");
        String response = HttpUtil.createGet(formattedUrl)
                .headerMap(headerMap, true)
                .execute()
                .body();
        return parsePriceMap(response);
    }

}
