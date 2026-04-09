package com.xiw.kuwei.service.stock.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.diboot.core.entity.AbstractEntity;
import com.diboot.core.util.BeanUtils;
import com.xiw.kuwei.calculator.MaCalculator;
import com.xiw.kuwei.calculator.MacdCalculator;
import com.xiw.kuwei.calculator.ThsTradeIndexChart;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import com.xiw.kuwei.entity.stock.StockInfo;
import com.xiw.kuwei.exception.LogicalException;
import com.xiw.kuwei.helper.fetcher.FetcherManager;
import com.xiw.kuwei.helper.fetcher.abstractFetcher;
import com.xiw.kuwei.service.stock.StockCommonService;
import com.xiw.kuwei.service.stock.StockDailyInfoService;
import com.xiw.kuwei.service.stock.StockInfoService;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import com.xiw.kuwei.vo.stock.StockInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockCommonServiceImpl implements StockCommonService {

    private static final BigDecimal BD_100 = new BigDecimal("100");


    @Resource
    private StockInfoService stockInfoService;

    @Resource
    private StockDailyInfoService stockDailyInfoService;

    @Override
    public void initStockInfo(String code, String name) {

        clearStockInfo(code);
        abstractFetcher fetcher = FetcherManager.getFetcher();
        StockInfo stockInfo = fetcher.getStockInfo(code, name);
        stockInfoService.createEntity(stockInfo);
        StockInfo one = stockInfoService
                .lambdaQuery()
                .eq(StockInfo::getCode, code)
                .eq(StockInfo::getName, name)
                .one();
        List<StockDailyInfo> stockDailyInfoList = fetcher.getStockDailyInfo(one);
        stockDailyInfoService.createEntities(stockDailyInfoList);
        AtomicReference<LocalDate> start = new AtomicReference<>();
        AtomicReference<LocalDate> end = new AtomicReference<>();
        stockDailyInfoList.stream()
                .map(StockDailyInfo::getDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .ifPresent(start::set);
        stockDailyInfoList.stream()
                .map(StockDailyInfo::getDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .ifPresent(end::set);
        log.info("初始化股票:【{}-{}】成功", name, code);
        log.info("股票:【{}-{}】共得到{}条数据，【{}～{}】的日期内的交易信息",
                name, code, stockDailyInfoList.size(), start.get(), end.get());

    }

    private void clearStockInfo(String code) {
        List<StockInfo> list = stockInfoService.lambdaQuery().eq(StockInfo::getCode, code).list();
        if (!list.isEmpty()) {
            List<String> idList = list.stream().map(AbstractEntity::getId).toList();
            stockDailyInfoService.deleteEntities(stockDailyInfoService.lambdaQuery()
                    .in(StockDailyInfo::getStockId, idList)
                    .getWrapper());
            stockInfoService.deleteEntities(idList);
        }
    }

    @Override
    public void updateStockDailyInfo() {
        List<StockDailyInfo> list = stockDailyInfoService.lambdaQuery().list();
        Map<String, List<StockDailyInfo>> stockInfoMap = list.stream()
                .collect(Collectors.groupingBy(StockDailyInfo::getStockId));
        stockInfoMap.forEach((k, v) -> {
            Map<LocalDate, List<StockDailyInfo>> dateListMap = v.stream()
                    .collect(Collectors.groupingBy(StockDailyInfo::getDate));
            List<LocalDate> dateList = dateListMap.keySet().stream().sorted((x, y) -> {
                if (x.isEqual(y)) {
                    return 0;
                } else if (x.isBefore(y)) {
                    return -1;
                } else {
                    return 1;
                }
            }).toList();
            BigDecimal yesterdayClosePrice = BigDecimal.ZERO;
            BigDecimal todayClosePrice;
            for (LocalDate localDate : dateList) {
                // 获取上一交易日收盘价
                List<StockDailyInfo> stockDailyInfos = dateListMap.get(localDate);
                todayClosePrice = stockDailyInfos.get(0).getTodayClosePrice();

                if (yesterdayClosePrice.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal changePrice = todayClosePrice.subtract(yesterdayClosePrice);
                    BigDecimal changePercentage = changePrice
                            .multiply(BD_100.divide(yesterdayClosePrice, 2, RoundingMode.HALF_EVEN));
                    for (StockDailyInfo e : stockDailyInfos) {
                        e.setChangePercentage(changePercentage);
                        e.setChangePrice(changePrice);
                        e.setYesterdayClosePrice(yesterdayClosePrice);
                    }
                }
                yesterdayClosePrice = todayClosePrice;
            }
        });
        stockDailyInfoService.updateEntities(list);
    }

    private StockInfoVO getExtraStockInfo(StockInfo stockInfo, int days) {
        StockInfoVO stockInfoVO = new StockInfoVO();
        BeanUtils.copyProperties(stockInfo, stockInfoVO);

        int totalDays = Integer.max(days * 2, days + 30);

        List<StockDailyInfo> list = stockDailyInfoService.lambdaQuery()
                .eq(StockDailyInfo::getStockId, stockInfo.getId())
                .orderByDesc(StockDailyInfo::getDate)
                .last("LIMIT " + totalDays)
                .list()
                .stream()
                // 转正序（MACD必须按时间递增算）
                .sorted(Comparator.comparing(StockDailyInfo::getDate))
                .toList();
        List<StockDailyInfoVO> voList = list.stream().map(e -> {
            StockDailyInfoVO vo = new StockDailyInfoVO();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).toList();

        MacdCalculator.calculate(voList);
        MaCalculator.calculate(voList);
        int right = voList.size();
        int left = Integer.max(0, right - days);
        stockInfoVO.setStockDailyInfoVOList(voList.subList(left, right));
        return stockInfoVO;
    }

    @Override
    public StockInfoVO getExtraStockInfo(String stockId, int days) {
        StockInfo stockInfo = stockInfoService.getEntity(stockId);
        if (stockInfo == null) {
            throw new NullPointerException("stockInfo is null");
        }
        return getExtraStockInfo(stockInfo, days);
    }

    @Override
    public void createChartByStockInfo(StockInfoVO stockInfoVO) {
        List<StockDailyInfoVO> stockDailyInfoVOList = stockInfoVO.getStockDailyInfoVOList();
        int size = stockDailyInfoVOList.size();
        String title = CharSequenceUtil.format("{}-{}-{}d-{}", stockInfoVO.getCode(), stockInfoVO.getName(), size, LocalDate.now());
        JFreeChart chart = ThsTradeIndexChart.createChart(stockInfoVO, title);

        try {
            ChartUtils.saveChartAsPNG(new File(StrUtil.format(title + ".png")), chart, 3000, 1800);
        } catch (IOException e) {
            throw new LogicalException(e);
        }
        log.info("生成完成：{}.png", title);

    }

    @Override
    public void createCharts(int days) {
        List<StockInfo> list = stockInfoService.lambdaQuery().list();
        list.forEach(e -> {
            StockInfoVO extraStockInfo = getExtraStockInfo(e, days);
            createChartByStockInfo(extraStockInfo);
        });
    }

}
