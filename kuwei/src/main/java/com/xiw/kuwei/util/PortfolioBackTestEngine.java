package com.xiw.kuwei.util;

import com.diboot.core.util.BeanUtils;
import com.xiw.kuwei.constant.DetectorEnum;
import com.xiw.kuwei.detector.DetectorFactory;
import com.xiw.kuwei.vo.backtest.*;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import com.xiw.kuwei.vo.stock.StockInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PortfolioBackTestEngine {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public static List<PortfolioBackTestResult> runPortfolioBackTest(
            List<StockInfoVO> stockList,
            BigDecimal initialCash) {
        return runPortfolioBackTest(stockList, initialCash, Arrays.asList(DetectorEnum.values()));
    }

    public static List<PortfolioBackTestResult> runPortfolioBackTest(
            List<StockInfoVO> stockList,
            BigDecimal initialCash,
            List<DetectorEnum> strategies) {
        List<PortfolioBackTestResult> resultList = new ArrayList<>();

        // ===== 1. 收集所有交易日 =====
        TreeSet<LocalDate> allDates = new TreeSet<>();
        Map<String, List<StockDailyInfoVO>> dataMap = new HashMap<>();

        for (StockInfoVO stock : stockList) {
            List<StockDailyInfoVO> list = stock.getStockDailyInfoVOList();
            dataMap.put(stock.getCode(), list);

            list.forEach(d -> allDates.add(d.getDate()));
        }
        for (DetectorEnum detectorEnum : strategies) {


            // ===== 2. 生成信号 =====
            // 策略-日期-股票
            Map<LocalDate, List<Signal>> dateSignalMap = buildSignalMap(stockList, detectorEnum);

            // ===== 3. 初始化 =====
            BigDecimal cash = initialCash;
            BigDecimal totalAsset = initialCash;

            Map<String, PositionInfo> positionMap = new HashMap<>();
            List<PortfolioDailyRecord> records = new ArrayList<>();

            // ===== 4. 逐日推进 =====
            for (LocalDate date : allDates) {

                List<TradeDetail> tradeDetailList = new ArrayList<>();
                List<Signal> signalList = new ArrayList<>();

                List<Signal> signals = dateSignalMap.getOrDefault(date, Collections.emptyList());
                for (StockInfoVO stock : stockList) {

                    String code = stock.getCode();
                    StockDailyInfoVO daily = findDaily(dataMap.get(code), date);
                    if (daily == null) continue;

                    BigDecimal price = daily.getTodayClosePrice();

                    PositionInfo pos = positionMap.computeIfAbsent(code, k -> new PositionInfo());
                    pos.setCode(code);
                    pos.setMaxPosition(totalAsset);
                    pos.setName(stock.getName());

                    // ===== 信号 =====

                    for (Signal signal : signals) {

                        BigDecimal strength = signal.getStrength();
                        if (strength == null || strength.compareTo(ZERO) <= 0) continue;

                        if (signal.getSign() == 0) {
                            // ===== 买 =====
                            BigDecimal buyAmount = cash.multiply(strength);
                            BigDecimal shares = buyAmount.divide(price, 0, RoundingMode.HALF_DOWN);
                            BigDecimal cost = shares.multiply(price);

                            if (cost.compareTo(ZERO) <= 0) continue;

                            cash = cash.subtract(cost);

                            pos.setPosition(pos.getPosition().add(shares));
                            pos.setTotalCost(pos.getTotalCost().add(cost));

                            tradeDetailList.add(buildTrade(code, "买", price, shares, cost, ZERO, signal.getDescription(), date));

                        } else {
                            // ===== 卖 =====
                            if (pos.getPosition().compareTo(ZERO) <= 0) continue;

                            BigDecimal avgCost = pos.getTotalCost()
                                    .divide(pos.getPosition(), 8, RoundingMode.HALF_UP);

                            // 最大可卖资金 = maxPosition * strength
                            BigDecimal targetSellValue = pos.getMaxPosition().multiply(strength);

                            // 转换为股数 = 金额 / 价格
                            BigDecimal targetSellShares = targetSellValue
                                    .divide(price, 0, RoundingMode.DOWN);

                            // 实际卖出股数 = min(目标股数, 当前持仓)
                            BigDecimal sellShares = targetSellShares.min(pos.getPosition());

                            if (sellShares.compareTo(ZERO) <= 0) continue;

                            BigDecimal revenue = sellShares.multiply(price);
                            BigDecimal pnl = price.subtract(avgCost).multiply(sellShares);

                            cash = cash.add(revenue);

                            pos.setPosition(pos.getPosition().subtract(sellShares));
                            pos.setTotalCost(pos.getTotalCost().subtract(avgCost.multiply(sellShares)));

                            tradeDetailList.add(
                                    buildTrade(code, "卖", price, sellShares, revenue, pnl, signal.getDescription(), date)
                            );
                        }
                        signalList.add(signal);
                    }

                    // ===== 更新持仓 =====
                    BigDecimal positionValue = pos.getPosition().multiply(price);

                    pos.setPrice(price);
                    pos.setPositionValue(positionValue);

                    if (pos.getPosition().compareTo(ZERO) > 0) {
                        pos.setAvgCost(
                                pos.getTotalCost().divide(pos.getPosition(), 8, RoundingMode.HALF_UP)
                        );
                    } else {
                        pos.setAvgCost(ZERO);
                        pos.setTotalCost(ZERO);
                    }
                }

                // ===== 组合资产 =====
                BigDecimal totalPositionValue = positionMap.values().stream()
                        .map(PositionInfo::getPositionValue)
                        .reduce(ZERO, BigDecimal::add);

                totalAsset = cash.add(totalPositionValue);

                PortfolioDailyRecord record = new PortfolioDailyRecord();
                record.setDate(date);
                record.setCash(cash);
                record.setTotalAsset(totalAsset);
                record.setTotalPositionValue(totalPositionValue);
                record.setPositionMap(clonePositionMap(positionMap));
                record.setTradeList(tradeDetailList);
                record.setSignalList(signalList);

                records.add(record);
            }
            resultList.add(buildResult(records, initialCash, dateSignalMap, detectorEnum));
        }


        return resultList;
    }

    // ===== 构建信号 =====
    private static Map<LocalDate, List<Signal>> buildSignalMap(
            List<StockInfoVO> stockList, DetectorEnum detectorEnum) {

        List<Signal> allSignals = new ArrayList<>();
        for (StockInfoVO stock : stockList) {
            allSignals.addAll(
                    DetectorFactory.getDetector(detectorEnum.getName())
                            .detectSignals(stock.getStockDailyInfoVOList(), stock.getCode())
            );
        }

        return allSignals.stream().collect(Collectors.groupingBy(s -> s.getDateTime().toLocalDate()));

    }

    private static StockDailyInfoVO findDaily(List<StockDailyInfoVO> list, LocalDate date) {
        return list.stream()
                .filter(d -> d.getDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    private static TradeDetail buildTrade(String code, String action,
                                          BigDecimal price,
                                          BigDecimal quantity,
                                          BigDecimal amount,
                                          BigDecimal pnl, String description, LocalDate date) {
        TradeDetail t = new TradeDetail();
        t.setCode(code);
        t.setAction(action);
        t.setPrice(price);
        t.setQuantity(quantity);
        t.setAmount(amount);
        t.setPnl(pnl);
        t.setDescription(description);
        t.setDate(date);
        return t;
    }

    private static Map<String, PositionInfo> clonePositionMap(Map<String, PositionInfo> src) {
        Map<String, PositionInfo> copy = new HashMap<>();
        for (Map.Entry<String, PositionInfo> e : src.entrySet()) {
            PositionInfo p = e.getValue();
            PositionInfo c = new PositionInfo();
            BeanUtils.copyProperties(p, c);
            copy.put(e.getKey(), c);
        }
        return copy;
    }

    private static PortfolioBackTestResult buildResult(List<PortfolioDailyRecord> list,
                                                       BigDecimal initialCash, Map<LocalDate, List<Signal>> dateSignalMap, DetectorEnum detectorEnum) {

        PortfolioBackTestResult r = new PortfolioBackTestResult();
        r.setPortfolioDailyRecordList(list);

        BigDecimal end = list.get(list.size() - 1).getTotalAsset();

        r.setStartAsset(initialCash);
        r.setEndAsset(end);

        r.setReturnRate(end.subtract(initialCash)
                .divide(initialCash, 6, RoundingMode.HALF_UP));

        r.setMaxDrawDown(calcMaxDrawdown(list));
        r.setSharpeRatio(calcSharpe(list));
        r.setSignalList(dateSignalMap.entrySet()
                .stream()
                // 1. 先按日期排序
                .sorted(Map.Entry.comparingByKey())
                // 2. 展开每一天的数据
                .flatMap(entry -> entry.getValue().stream()
                        // 3. 每一天内部按 code 排序
                        .sorted(Comparator.comparing(Signal::getCode))
                )
                // 4. 收集为有序 List
                .toList());
        r.setTradeDetailList(list.stream()
                .map(PortfolioDailyRecord::getTradeList)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(TradeDetail::getDate)).toList());
        r.setStrategy(detectorEnum.getDetectorInterface().getDetectorName());
        return r;
    }

    private static BigDecimal calcMaxDrawdown(List<PortfolioDailyRecord> list) {

        BigDecimal peak = list.get(0).getTotalAsset();
        BigDecimal max = ZERO;

        for (PortfolioDailyRecord r : list) {
            if (r.getTotalAsset().compareTo(peak) > 0) {
                peak = r.getTotalAsset();
            }

            BigDecimal dd = peak.subtract(r.getTotalAsset())
                    .divide(peak, 6, RoundingMode.HALF_UP);

            if (dd.compareTo(max) > 0) {
                max = dd;
            }
        }
        return max;
    }

    private static BigDecimal calcSharpe(List<PortfolioDailyRecord> list) {

        if (list.size() < 2) return ZERO;

        List<Double> returns = new ArrayList<>();

        for (int i = 1; i < list.size(); i++) {
            BigDecimal prev = list.get(i - 1).getTotalAsset();
            BigDecimal curr = list.get(i).getTotalAsset();

            double r = curr.subtract(prev)
                    .divide(prev, 8, RoundingMode.HALF_UP)
                    .doubleValue();

            returns.add(r);
        }

        double avg = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double std = Math.sqrt(
                returns.stream().mapToDouble(r -> Math.pow(r - avg, 2))
                        .average().orElse(0)
        );

        return std == 0 ? ZERO :
                BigDecimal.valueOf(avg / std * Math.sqrt(252));
    }

}