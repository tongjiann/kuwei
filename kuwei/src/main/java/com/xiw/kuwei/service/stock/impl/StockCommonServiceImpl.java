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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockCommonServiceImpl implements StockCommonService {

    @Value("${diboot.file.storage-directory}")
    private String resourcesDirPath;

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
    @Transactional
    public void updateStockInfo() {
        List<StockInfo> stockInfoList = stockInfoService.lambdaQuery().list();
        List<StockDailyInfo> stockDailyInfoList = stockDailyInfoService.lambdaQuery().list();
        Map<String, LocalDate> stockIdLastDataFreshTimeMap = stockDailyInfoList.stream()
                .filter(e -> e.getStockId() != null)
                .collect(Collectors.toMap(StockDailyInfo::getStockId, StockDailyInfo::getDate, (x, y) -> {
                    if (x.isAfter(y)) {
                        return x;
                    }
                    return y;
                }));
        stockInfoList.forEach(stockInfo -> {
            stockInfo.setLatestDataFreshTime(stockIdLastDataFreshTimeMap.get(stockInfo.getId()));
        });
        stockInfoService.updateEntities(stockInfoList);
    }

    @Override
    @Transactional
    public void updateStockDailyInfo() {
        log.info("开始更新每日信息");
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
            ChartUtils.saveChartAsPNG(new File(StrUtil.format(resourcesDirPath + title + ".png")), chart, 3000, 1800);
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
            stockId = initStockInfo(code);
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
            ChartUtils.saveChartAsPNG(new File(CharSequenceUtil.format(resourcesDirPath + "{}-{}_asset.png", code, name)), chart, 3000, 1800);
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
            initStockInfo(code);
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
            ChartUtils.saveChartAsPNG(new File(CharSequenceUtil.format(resourcesDirPath + "{}-{}多策略收益对比.png", code, stockInfoVO.getName())), chart, 3000, 1800);
        } catch (IOException e) {
            throw new LogicalException(e);
        }

    }

    private String initStockInfo(String code) {
        return initStockInfo(code, code);
    }

    @Override
    @Transactional
    public void syncDailyInfo() {
        log.info("开始同步股票信息");
        // 1. 获取所有需要同步的股票
        List<StockInfo> list = stockInfoService.lambdaQuery().list();
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 2. 逐只股票处理（可考虑并行优化，但需注意数据库连接和接口限流）
        for (StockInfo stockInfo : list) {
            LocalDate latestFreshTime = stockInfo.getLatestDataFreshTime();
            LocalDate today = LocalDate.now();

            // 2.1 计算需要同步的天数
            int daysToSync;
            if (latestFreshTime == null) {
                // 首次同步：默认拉取最近30天数据（可根据业务调整）
                daysToSync = 30;
            } else {
                // 增量同步：从 latestFreshTime 的下一天开始，到今日为止
                LocalDate startDate = latestFreshTime.plusDays(1);
                if (startDate.isAfter(today)) {
                    // 数据已是最新，无需同步
                    continue;
                }
                daysToSync = (int) ChronoUnit.DAYS.between(startDate, today) + 1;
            }
            abstractFetcher fetcher = FetcherManager.getFetcher();
            // 2.2 调用 fetcher 获取数据
            List<StockDailyInfo> dailyInfoList;
            String code = stockInfo.getCode();
            String name = stockInfo.getName();
            List<StockDailyInfo> toSaveList;
            try {
                dailyInfoList = fetcher.getStockDailyInfo(stockInfo, daysToSync);
                toSaveList = dailyInfoList.stream()
                        .filter(e -> e.getDate().isAfter(latestFreshTime))
                        .toList();
            } catch (Exception e) {
                log.error("同步股票  {}-{} 每日数据失败，跳过该股票", name, code, e);
                continue;
            }

            if (CollUtil.isEmpty(toSaveList)) {
                log.info("股票  {}-{} 无新增数据", name, code);
                continue;
            }


            // 2.3 批量保存或更新每日数据（假设使用 MyBatis-Plus 的 saveOrUpdateBatch）
            boolean saved = stockDailyInfoService.createEntities(toSaveList);
            if (!saved) {
                log.error("保存股票 {}-{} 每日数据失败", name, code);
                continue;
            } else {
                log.error("保存股票 {}-{} 每日数据成功，共新增{}条数据", name, code, toSaveList.size());
            }

            // 2.4 更新 stockInfo 的 latestDataFreshTime 为本次同步的最大日期
            LocalDate maxDate = dailyInfoList.stream()
                    .map(StockDailyInfo::getDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            if (maxDate != null && (latestFreshTime == null || maxDate.isAfter(latestFreshTime))) {
                stockInfo.setLatestDataFreshTime(maxDate);
                boolean updated = stockInfoService.updateEntity(stockInfo);
                if (!updated) {
                    log.warn("更新股票  {}-{} 的最新数据时间失败，但不影响数据同步", name, code);
                }
            }
        }
        updateStockDailyInfo();
    }

}
