package com.xiw.kuwei.service.stock.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.diboot.core.entity.AbstractEntity;
import com.diboot.core.util.BeanUtils;
import com.xiw.kuwei.calculator.MaCalculator;
import com.xiw.kuwei.calculator.MacdCalculator;
import com.xiw.kuwei.chart.BackTestChart;
import com.xiw.kuwei.chart.ThsTradeIndexChart;
import com.xiw.kuwei.constant.DetectorEnum;
import com.xiw.kuwei.detector.*;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import com.xiw.kuwei.entity.stock.StockInfo;
import com.xiw.kuwei.exception.LogicalException;
import com.xiw.kuwei.helper.fetcher.FetcherManager;
import com.xiw.kuwei.helper.fetcher.abstractFetcher;
import com.xiw.kuwei.service.stock.StockCommonService;
import com.xiw.kuwei.service.stock.StockDailyInfoService;
import com.xiw.kuwei.service.stock.StockInfoService;
import com.xiw.kuwei.util.BackTestEngine;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import com.xiw.kuwei.vo.stock.StockInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
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
    public String initStockInfo(String code, String name) {

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

        return one.getId();
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

    @Override
    public void macdDivergence(String stockId) {
        StockInfoVO stockInfoVO = getBaseStockInfo(stockId);

        List<StockDailyInfoVO> voList = stockInfoVO.getStockDailyInfoVOList();
        // 2. 计算 MACD 指标
        MacdCalculator.calculate(voList);

        // 3. 识别背离
        DivergenceDetector.DivergenceDetectionResult divergences = DivergenceDetector.detectAll(voList);

        // 4. 打印识别结果
        log.info("=== 背离识别结果 ===");
        log.info("顶背离数量: " + divergences.topDivergences.size());
        for (DivergenceResult d : divergences.topDivergences) {
            log.info(d.toString());
        }
        log.info("底背离数量: " + divergences.bottomDivergences.size());
        for (DivergenceResult d : divergences.bottomDivergences) {
            log.info(d.toString());
        }

    }

    private @NotNull StockInfoVO getBaseStockInfoByCode(String code) {
        StockInfo stockInfo = stockInfoService.lambdaQuery().eq(StockInfo::getCode, code).one();
        if (stockInfo == null) {
            throw new NullPointerException("stockInfo is null");
        }
        return doGetBaseStockInfo(stockInfo);

    }
    private @NotNull StockInfoVO getBaseStockInfo(String stockId) {
        StockInfo stockInfo = stockInfoService.getEntity(stockId);
        if (stockInfo == null) {
            throw new NullPointerException("stockInfo is null");
        }
        return doGetBaseStockInfo(stockInfo);
    }

    private @NotNull StockInfoVO doGetBaseStockInfo(StockInfo stockInfo) {
        StockInfoVO stockInfoVO = new StockInfoVO();
        BeanUtils.copyProperties(stockInfo, stockInfoVO);

        int days = 200 * 3;
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
        stockInfoVO.setStockDailyInfoVOList(voList);
        return stockInfoVO;
    }


    @Override
    public void macdSignalByCode(String code) {
        String stockId;
        if (code == null) {
            throw new NullPointerException("code is null");
        }
        List<StockInfo> list = stockInfoService.lambdaQuery().eq(StockInfo::getCode, code).list();
        if (CollUtil.isEmpty(list)) {
            stockId = initStockInfo(code, "自动初始化");
        } else {
            stockId = list.get(0).getId();
        }
        if (stockId != null) {
            macdSignal(stockId);
        }
    }

    @Override
    public void macdSignal(String stockId) {
        StockInfoVO stockInfoVO = getBaseStockInfo(stockId);
        doMacdSignal(stockInfoVO);
    }

    private void doMacdSignal(StockInfoVO stockInfoVO) {
        // 2. 计算 MACD 指标
        List<StockDailyInfoVO> voList = stockInfoVO.getStockDailyInfoVOList();
        MacdCalculator.calculate(voList);

        // 3. 👇 使用新的信号检测器（忽略背离识别器）
        String code = stockInfoVO.getCode();
        String name = stockInfoVO.getName();
        List<Signal> signals = DetectorFactory.getDetector(DetectorEnum.MACD.getType()).detectSignals(voList, code);

        // 4. 打印信号
        log.info("==== {}-{} MACD 交易信号 ====", code, name);
        for (Signal s : signals) {
            log.info(s.toString());
        }

        List<BackTestRecord> backTestRecordList = BackTestEngine.runBackTest(signals, new BigDecimal("1000000"), code);
        log.info("==== {}-{} MACD 回测交易信息 ====", code, name);
        for (BackTestRecord backTestRecord : backTestRecordList) {
            log.info(backTestRecord.toString());
        }

        // 和上证指数做对比
        StockInfoVO sh000001 = getBaseStockInfoByCode("sh000001");
        Map<String, List<StockDailyInfoVO>> benchmarkMap = new HashMap<>();
        benchmarkMap.put(sh000001.getCode(), sh000001.getStockDailyInfoVOList());
        benchmarkMap.put(stockInfoVO.getCode(), stockInfoVO.getStockDailyInfoVOList());
        JFreeChart chart = BackTestChart.createComparisonChart(backTestRecordList, benchmarkMap, null);
        try {
            ChartUtils.saveChartAsPNG(new File(CharSequenceUtil.format("{}-{}_asset.png", code, name)), chart, 3000, 1800);
        } catch (IOException e) {
            throw new LogicalException(e);
        }
    }

    @Override
    public void multiTest(String code) {

        if (code == null) {
            throw new NullPointerException("code is null");
        }
        List<StockInfo> list = stockInfoService.lambdaQuery().eq(StockInfo::getCode, code).list();
        if (CollUtil.isEmpty(list)) {
            initStockInfo(code, "自动初始化");
        }

        StockInfoVO stockInfoVO = getBaseStockInfoByCode(code);
        List<StockDailyInfoVO> voList = stockInfoVO.getStockDailyInfoVOList();

        DetectorEnum[] values = DetectorEnum.values();
        Map<String, List<Signal>> signalMap = new HashMap<>();
        for (DetectorEnum value : values) {
            signalMap.put(value.getName(), DetectorFactory.getDetector(value.getType())
                    .detectSignals(voList, code));

        }

        Map<String, List<BackTestRecord>> resultMap = BackTestEngine.runMultiStrategyBackTest(signalMap, new BigDecimal("1000000"), code);
        // 和上证指数做对比
        StockInfoVO sh000001 = getBaseStockInfoByCode("sh000001");
        Map<String, List<StockDailyInfoVO>> benchmarkMap = new HashMap<>();
        benchmarkMap.put(sh000001.getCode(), sh000001.getStockDailyInfoVOList());
        benchmarkMap.put(stockInfoVO.getCode(), stockInfoVO.getStockDailyInfoVOList());
        JFreeChart chart = BackTestChart.createMultiStrategyComparisonChart(resultMap, benchmarkMap, null);
        try {
            ChartUtils.saveChartAsPNG(new File(CharSequenceUtil.format("{}-{}多策略收益对比.png", code, stockInfoVO.getName())), chart, 3000, 1800);
        } catch (IOException e) {
            throw new LogicalException(e);
        }

    }

}
