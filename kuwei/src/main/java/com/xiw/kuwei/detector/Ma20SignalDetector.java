package com.xiw.kuwei.detector;

import com.xiw.kuwei.annotation.Detector;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 20日均线交易信号识别器
 * 规则：收盘价上穿20日均线 → 买入（强度=1）
 * 收盘价下穿20日均线 → 卖出（强度=1）
 */
@Detector(name = "20日均线交易信号识别器")
public class Ma20SignalDetector implements DetectorInterface {

    private static final int MA_PERIOD = 20;

    private static final BigDecimal STRENGTH = BigDecimal.ONE;

    /**
     * 确保数据中 MA20 已计算，若未计算则动态计算并填充
     */
    private static void ensureMa20Calculated(List<StockDailyInfoVO> data) {
        for (int i = 0; i < data.size(); i++) {
            StockDailyInfoVO vo = data.get(i);
            if (vo.getMaInfo() == null) {
                vo.setMaInfo(new com.xiw.kuwei.calculator.MaInfo());
            }
            if (vo.getMaInfo().getMa20() != null) {
                continue; // 已存在，跳过
            }

            if (i >= MA_PERIOD - 1) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - MA_PERIOD + 1; j <= i; j++) {
                    sum = sum.add(data.get(j).getTodayClosePrice());
                }
                BigDecimal ma20 = sum.divide(BigDecimal.valueOf(MA_PERIOD), 4, RoundingMode.HALF_UP);
                vo.getMaInfo().setMa20(ma20);
            }
        }
    }

    /**
     * 构造 Signal 对象
     */
    private static Signal buildSignal(int sign, String code, StockDailyInfoVO vo, String desc) {
        Signal signal = new Signal();
        signal.setSign(sign);
        signal.setCode(code);
        signal.setDescription(desc);
        signal.setPrice(vo.getTodayClosePrice());
        LocalDate date = vo.getDate();
        signal.setDateTime(LocalDateTime.of(date, LocalTime.of(15, 0)));
        signal.setStrength(STRENGTH);
        return signal;
    }

    /**
     * 检测所有信号
     *
     * @param data 包含均线信息的日线数据（需已计算 MA20）
     * @param code 股票代码
     * @return 信号列表
     */
    public List<Signal> detectSignals(List<StockDailyInfoVO> data, String code) {
        List<Signal> signals = new ArrayList<>();

        if (data == null || data.size() < MA_PERIOD + 1) return signals;

        // 1. 确保数据中 MA20 已计算，若未计算则动态计算
        ensureMa20Calculated(data);

        // 2. 遍历寻找穿越信号
        for (int i = 1; i < data.size(); i++) {
            StockDailyInfoVO prev = data.get(i - 1);
            StockDailyInfoVO curr = data.get(i);

            BigDecimal prevClose = prev.getTodayClosePrice();
            BigDecimal currClose = curr.getTodayClosePrice();
            BigDecimal prevMa = prev.getMaInfo() != null ? prev.getMaInfo().getMa20() : null;
            BigDecimal currMa = curr.getMaInfo() != null ? curr.getMaInfo().getMa20() : null;

            if (prevClose == null || currClose == null || prevMa == null || currMa == null) {
                continue;
            }

            // 上穿：前一日收盘 <= 前一日MA20 且 当日收盘 > 当日MA20
            boolean crossAbove = prevClose.compareTo(prevMa) <= 0 && currClose.compareTo(currMa) > 0;

            // 下穿：前一日收盘 >= 前一日MA20 且 当日收盘 < 当日MA20
            boolean crossBelow = prevClose.compareTo(prevMa) >= 0 && currClose.compareTo(currMa) < 0;

            if (crossAbove) {
                signals.add(buildSignal(0, code, curr, "收盘上穿20日均线"));
            } else if (crossBelow) {
                signals.add(buildSignal(1, code, curr, "收盘下穿20日均线"));
            }
        }

        // 去重与排序
        Set<String> seenKeys = new LinkedHashSet<>();
        List<Signal> uniqueSignals = new ArrayList<>();
        DateTimeFormatter keyFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        for (Signal s : signals) {
            String key = s.getDateTime().toLocalDate().format(keyFormatter)
                    + "|" + s.getSign() + "|" + s.getDescription();
            if (seenKeys.add(key)) {
                uniqueSignals.add(s);
            }
        }

        uniqueSignals.sort(Comparator.comparing(Signal::getDateTime));
        return uniqueSignals;
    }

}