package com.xiw.kuwei.detector;

import com.xiw.kuwei.calculator.MacdInfo;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * MACD 金叉/死叉交易信号识别器（BigDecimal 版本）
 * 输出标准 Signal 对象
 */
public class MacdSignalDetector {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * 检测所有信号
     *
     * @param data 包含 MACD 信息的日线数据
     * @param code 股票代码
     * @return 信号列表
     */
    public static List<Signal> detectSignals(List<StockDailyInfoVO> data, String code) {
        List<Signal> signals = new ArrayList<>();

        if (data == null || data.size() < 2) return signals;

        // 预处理：提取有效的 DIF/DEA 序列，并记录有效索引
        List<Integer> validIndices = new ArrayList<>();
        List<BigDecimal> difList = new ArrayList<>();
        List<BigDecimal> deaList = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            StockDailyInfoVO vo = data.get(i);
            MacdInfo macd = vo.getMacdInfo();
            if (macd != null) {
                validIndices.add(i);
                difList.add(macd.getDif());
                deaList.add(macd.getDea());
            }
        }

        int n = validIndices.size();
        if (n < 2) return signals;

        int[] idx = validIndices.stream().mapToInt(Integer::intValue).toArray();
        BigDecimal[] dif = difList.toArray(new BigDecimal[0]);
        BigDecimal[] dea = deaList.toArray(new BigDecimal[0]);

        // ========== 买入信号检测 ==========
        detectDoubleGoldenCrossWithDeathBelowZeroAndBreakAboveZero(data, idx, dif, dea, code, signals, new BigDecimal("0.5"));
        detectConsecutiveGoldenCrossBelowZero(data, idx, dif, dea, code, signals, new BigDecimal("0.25"));
        detectGoldenCrossAndBreakAboveZero(data, idx, dif, dea, code, signals, new BigDecimal("0.25"));
        // 可扩展其他买入信号...

        // ========== 卖出信号检测 ==========
        detectConsecutiveDeathCrossAboveZero(data, idx, dif, dea, code, signals, new BigDecimal("0.5"));
        detectDeathCrossAndBreakBelowZero(data, idx, dif, dea, code, signals, BigDecimal.ONE);

        // 去重与排序（按日期时间）
        Set<String> seenKeys = new LinkedHashSet<>();
        List<Signal> uniqueSignals = new ArrayList<>();
        DateTimeFormatter keyFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        for (Signal s : signals) {
            // 唯一键：日期 + 买卖方向 + 描述
            String key = s.getDateTime().toLocalDate().format(keyFormatter)
                    + "|" + s.getSign() + "|" + s.getDescription();
            if (seenKeys.add(key)) {
                uniqueSignals.add(s);
            }
        }

        uniqueSignals.sort(Comparator.comparing(Signal::getDateTime));
        return uniqueSignals;
    }


    /**
     * 0轴下双金叉夹死叉 + DIF突破0轴，且期间未触及0轴
     */
    private static void detectDoubleGoldenCrossWithDeathBelowZeroAndBreakAboveZero(
            List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea,
            String code, List<Signal> signals, BigDecimal strength) {

        List<Integer> goldenCrossBelowZeroPositions = new ArrayList<>();
        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0) {
                if (dif[i].compareTo(ZERO) < 0 && dea[i].compareTo(ZERO) < 0) {
                    goldenCrossBelowZeroPositions.add(i);
                }
            }
        }

        for (int k = 0; k < goldenCrossBelowZeroPositions.size() - 1; k++) {
            int firstGolden = goldenCrossBelowZeroPositions.get(k);
            int secondGolden = goldenCrossBelowZeroPositions.get(k + 1);

            boolean hasDeathCrossBelowZero = false;
            for (int i = firstGolden + 1; i < secondGolden; i++) {
                if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0) {
                    if (dif[i].compareTo(ZERO) < 0 && dea[i].compareTo(ZERO) < 0) {
                        hasDeathCrossBelowZero = true;
                        break;
                    }
                }
            }
            if (!hasDeathCrossBelowZero) continue;

            for (int j = secondGolden + 1; j < dif.length; j++) {
                if (dif[j - 1].compareTo(ZERO) < 0 && dif[j].compareTo(ZERO) >= 0) {
                    boolean everAboveZero = false;
                    for (int t = firstGolden; t < j; t++) {
                        if (dif[t].compareTo(ZERO) >= 0 || dea[t].compareTo(ZERO) >= 0) {
                            everAboveZero = true;
                            break;
                        }
                    }
                    if (!everAboveZero) {
                        int dataIndex = idx[j];
                        StockDailyInfoVO vo = data.get(dataIndex);
                        signals.add(buildSignal(0, code, vo,
                                "0轴下双金叉夹死叉+突破0轴", dif[j], dea[j], strength));
                    }
                    break;
                }
            }
        }
    }

    /**
     * 连续两次0轴上死叉
     */
    private static void detectConsecutiveDeathCrossAboveZero(
            List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea,
            String code, List<Signal> signals, BigDecimal strength) {

        List<Integer> deathCrossAboveZeroPositions = new ArrayList<>();
        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0) {
                if (dif[i].compareTo(ZERO) > 0 && dea[i].compareTo(ZERO) > 0) {
                    deathCrossAboveZeroPositions.add(i);
                }
            }
        }

        for (int k = 0; k < deathCrossAboveZeroPositions.size() - 1; k++) {
            int first = deathCrossAboveZeroPositions.get(k);
            int second = deathCrossAboveZeroPositions.get(k + 1);

            boolean hasGoldenCrossBetween = false;
            for (int i = first + 1; i < second; i++) {
                if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0) {
                    hasGoldenCrossBetween = true;
                    break;
                }
            }
            if (hasGoldenCrossBetween) {
                int dataIndex = idx[second];
                StockDailyInfoVO vo = data.get(dataIndex);
                signals.add(buildSignal(1, code, vo,
                        "连续2次0轴上死叉", dif[second], dea[second], strength));
            }
        }
    }

    /**
     * 一次0轴上死叉 + DIF跌破0轴
     */
    private static void detectDeathCrossAndBreakBelowZero(
            List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea,
            String code, List<Signal> signals, BigDecimal strength) {

        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0 &&
                    dif[i].compareTo(ZERO) > 0 && dea[i].compareTo(ZERO) > 0) {
                for (int j = i + 1; j < dif.length; j++) {
                    if (dif[j - 1].compareTo(ZERO) > 0 && dif[j].compareTo(ZERO) <= 0) {
                        int dataIndex = idx[j];
                        StockDailyInfoVO vo = data.get(dataIndex);
                        signals.add(buildSignal(1, code, vo,
                                "0轴上死叉后DIF跌破0轴", dif[j], dea[j], strength));
                        break;
                    }
                }
            }
        }
    }

    private static void detectConsecutiveGoldenCrossBelowZero(
            List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea,
            String code, List<Signal> signals, BigDecimal strength) {

        List<Integer> goldenCrossBelowZeroPositions = new ArrayList<>();
        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0) {
                if (dif[i].compareTo(ZERO) < 0 && dea[i].compareTo(ZERO) < 0) {
                    goldenCrossBelowZeroPositions.add(i);
                }
            }
        }

        for (int k = 0; k < goldenCrossBelowZeroPositions.size() - 1; k++) {
            int first = goldenCrossBelowZeroPositions.get(k);
            int second = goldenCrossBelowZeroPositions.get(k + 1);

            boolean hasDeathCrossBetween = false;
            for (int i = first + 1; i < second; i++) {
                if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0) {
                    hasDeathCrossBetween = true;
                    break;
                }
            }

            if (hasDeathCrossBetween) {
                int dataIndex = idx[second];
                StockDailyInfoVO vo = data.get(dataIndex);
                signals.add(buildSignal(0, code, vo,
                        "连续2次0轴下金叉", dif[second], dea[second], strength));
            }
        }
    }

    private static void detectGoldenCrossAndBreakAboveZero(
            List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea,
            String code, List<Signal> signals, BigDecimal strength) {

        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0 &&
                    dif[i].compareTo(ZERO) < 0 && dea[i].compareTo(ZERO) < 0) {

                for (int j = i + 1; j < dif.length; j++) {
                    if (dif[j - 1].compareTo(ZERO) < 0 && dif[j].compareTo(ZERO) >= 0) {
                        int dataIndex = idx[j];
                        StockDailyInfoVO vo = data.get(dataIndex);
                        signals.add(buildSignal(0, code, vo,
                                "0轴下金叉后DIF突破0轴", dif[j], dea[j], strength));
                        break;
                    }
                }
            }
        }
    }

    /**
     * 构造 Signal 对象
     *
     * @param sign 0-买入，1-卖出
     * @param code 股票代码
     * @param vo   当日数据
     * @param desc 描述
     * @param dif  DIF值（可选，目前未存入Signal）
     * @param dea  DEA值
     * @return Signal 对象
     */
    private static Signal buildSignal(int sign, String code, StockDailyInfoVO vo,
                                      String desc, BigDecimal dif, BigDecimal dea, BigDecimal strength) {
        Signal signal = new Signal();
        signal.setSign(sign);
        signal.setCode(code);
        signal.setDescription(desc + "DIF:" + dif.setScale(2, RoundingMode.HALF_EVEN) + " DEA:" + dea.setScale(2, RoundingMode.HALF_EVEN));
        // 操作价格：默认使用收盘价
        signal.setPrice(vo.getTodayClosePrice());
        // 日期时间：取当日收盘时间（15:00）作为操作时间
        LocalDate date = vo.getDate();
        signal.setDateTime(LocalDateTime.of(date, LocalTime.of(15, 0)));
        // 强度暂未计算，设为0（后续可扩展）
        signal.setStrength(strength);
        return signal;
    }

}