package com.xiw.kuwei.detector;

import com.xiw.kuwei.annotation.Detector;
import com.xiw.kuwei.calculator.MacdCalculator;
import com.xiw.kuwei.calculator.MacdInfo;
import com.xiw.kuwei.vo.backtest.Signal;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * MACD 金叉/死叉交易信号识别器（BigDecimal 版本）
 * 输出标准 Signal 对象
 */
@Detector(name = "MACD 金叉/死叉交易信号识别器")
public class MacdSignalDetector implements DetectorInterface {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private String detectorName = null;

    /**
     * 0轴下双金叉夹死叉 + DIF突破0轴，且期间未触及0轴
     */
    private void detectDoubleGoldenCrossWithDeathBelowZeroAndBreakAboveZero(
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
                        String desc = "0轴下双金叉夹死叉+突破0轴 DIF:" + dif[j].setScale(2, RoundingMode.HALF_EVEN) + " DEA:" + dea[j].setScale(2, RoundingMode.HALF_EVEN);
                        signals.add(buildSignal(0, code, vo, strength, desc));
                    }
                    break;
                }
            }
        }
    }

    /**
     * 连续两次0轴上死叉
     */
    private void detectConsecutiveDeathCrossAboveZero(
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
                String desc = "连续2次0轴上死叉:" + dif[second].setScale(2, RoundingMode.HALF_EVEN) + " DEA:" + dea[second].setScale(2, RoundingMode.HALF_EVEN);
                signals.add(buildSignal(1, code, vo, strength, desc));
            }
        }
    }

    /**
     * 一次0轴上死叉 + DIF跌破0轴
     */
    private void detectDeathCrossAndBreakBelowZero(
            List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea,
            String code, List<Signal> signals, BigDecimal strength) {

        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0 &&
                    dif[i].compareTo(ZERO) > 0 && dea[i].compareTo(ZERO) > 0) {
                for (int j = i + 1; j < dif.length; j++) {
                    if (dif[j - 1].compareTo(ZERO) > 0 && dif[j].compareTo(ZERO) <= 0) {
                        int dataIndex = idx[j];
                        StockDailyInfoVO vo = data.get(dataIndex);
                        String desc = "0轴上死叉后DIF跌破0轴DIF:" + dif[j].setScale(2, RoundingMode.HALF_EVEN) + " DEA:" + dea[j].setScale(2, RoundingMode.HALF_EVEN);
                        signals.add(buildSignal(1, code, vo, strength, desc));

                        break;
                    }
                }
            }
        }
    }

    private void detectConsecutiveGoldenCrossBelowZero(
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
                String desc = "连续2次0轴下金叉:" + dif[second].setScale(2, RoundingMode.HALF_EVEN) + " DEA:" + dea[second].setScale(2, RoundingMode.HALF_EVEN);
                signals.add(buildSignal(0, code, vo, strength, desc));
            }
        }
    }

    private void detectGoldenCrossAndBreakAboveZero(
            List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea,
            String code, List<Signal> signals, BigDecimal strength) {

        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0 &&
                    dif[i].compareTo(ZERO) < 0 && dea[i].compareTo(ZERO) < 0) {

                for (int j = i + 1; j < dif.length; j++) {
                    if (dif[j - 1].compareTo(ZERO) < 0 && dif[j].compareTo(ZERO) >= 0) {
                        int dataIndex = idx[j];
                        StockDailyInfoVO vo = data.get(dataIndex);
                        String desc = "0轴下金叉后DIF突破0轴:" + dif[j].setScale(2, RoundingMode.HALF_EVEN) + " DEA:" + dea[j].setScale(2, RoundingMode.HALF_EVEN);
                        signals.add(buildSignal(0, code, vo, strength, desc));
                        break;
                    }
                }
            }
        }
    }

    /**
     * 检测所有信号
     *
     * @param data 包含 MACD 信息的日线数据
     * @param code 股票代码
     * @return 信号列表
     */
    @Override
    public List<Signal> doDetectSignals(List<StockDailyInfoVO> data, String code) {

        MacdCalculator.calculate(data);
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
        return signals;
    }

    @Override
    public String getDetectorName() {
        if (detectorName == null) {
            detectorName = this.getClass().getAnnotation(Detector.class).name();
        }
        return detectorName;
    }

}