package com.xiw.kuwei.detector;

import cn.hutool.core.text.CharSequenceUtil;
import com.xiw.kuwei.annotation.Detector;
import com.xiw.kuwei.calculator.MacdCalculator;
import com.xiw.kuwei.calculator.MacdInfo;
import com.xiw.kuwei.vo.backtest.Signal;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Detector(name = "MACD 金叉/死叉交易信号识别器")
public class MacdSignalDetector implements DetectorInterface {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final int fast;

    private final int slow;

    private final int signal;

    private final MacdCalculator macdCalculator;

    private String detectorName = null;

    public MacdSignalDetector(Integer fast, Integer slow, Integer signal) {
        this.fast = fast;
        this.slow = slow;
        this.signal = signal;
        this.macdCalculator = new MacdCalculator(fast, slow, signal);
    }


    /**
     * 0轴下双金叉夹死叉 + DIF突破0轴，且期间未触及0轴
     */
    private void detectDoubleGoldenCrossWithDeathBelowZeroAndBreakAboveZero(List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea, String code, List<Signal> signals, BigDecimal strength) {

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
                        StockDailyInfoVO vo = data.get(idx[j]);
                        String desc = "0轴下双金叉夹死叉+突破0轴 DIF:" + dif[j].setScale(2, RoundingMode.HALF_EVEN);

                        signals.add(buildSignal(0, code, vo, strength, desc));
                    }
                    break;
                }
            }
        }
    }

    /**
     * 连续2次0轴上死叉
     */
    private void detectConsecutiveDeathCrossAboveZero(List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea, String code, List<Signal> signals, BigDecimal strength) {

        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0) {
                if (dif[i].compareTo(ZERO) > 0 && dea[i].compareTo(ZERO) > 0) {
                    list.add(i);
                }
            }
        }

        for (int k = 0; k < list.size() - 1; k++) {
            int first = list.get(k);
            int second = list.get(k + 1);

            boolean hasGolden = false;
            for (int i = first + 1; i < second; i++) {
                if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0) {
                    hasGolden = true;
                    break;
                }
            }

            if (hasGolden) {
                StockDailyInfoVO vo = data.get(idx[second]);
                signals.add(buildSignal(1, code, vo, strength, "连续2次0轴上死叉"));
            }
        }
    }

    /**
     * 死叉后跌破0轴
     */
    private void detectDeathCrossAndBreakBelowZero(List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea, String code, List<Signal> signals, BigDecimal strength) {

        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0 && dif[i].compareTo(ZERO) > 0) {

                for (int j = i + 1; j < dif.length; j++) {
                    if (dif[j - 1].compareTo(ZERO) > 0 && dif[j].compareTo(ZERO) <= 0) {
                        StockDailyInfoVO vo = data.get(idx[j]);
                        signals.add(buildSignal(1, code, vo, strength, "死叉后跌破0轴"));
                        break;
                    }
                }
            }
        }
    }

    /**
     * 金叉后突破0轴
     */
    private void detectGoldenCrossAndBreakAboveZero(List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea, String code, List<Signal> signals, BigDecimal strength) {

        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0 && dif[i].compareTo(ZERO) < 0) {

                for (int j = i + 1; j < dif.length; j++) {
                    if (dif[j - 1].compareTo(ZERO) < 0 && dif[j].compareTo(ZERO) >= 0) {
                        StockDailyInfoVO vo = data.get(idx[j]);
                        signals.add(buildSignal(0, code, vo, strength, "金叉后突破0轴"));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public List<Signal> doDetectSignals(List<StockDailyInfoVO> data, String code) {

        macdCalculator.calculate(data);

        List<Signal> signals = new ArrayList<>();
        if (data == null || data.size() < 2) return signals;

        List<Integer> validIndices = new ArrayList<>();
        List<BigDecimal> difList = new ArrayList<>();
        List<BigDecimal> deaList = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            MacdInfo macd = data.get(i).getMacdInfo();
            if (macd != null) {
                validIndices.add(i);
                difList.add(macd.getDif());
                deaList.add(macd.getDea());
            }
        }

        if (validIndices.size() < 2) return signals;

        int[] idx = validIndices.stream().mapToInt(Integer::intValue).toArray();
        BigDecimal[] dif = difList.toArray(new BigDecimal[0]);
        BigDecimal[] dea = deaList.toArray(new BigDecimal[0]);

        // ========== 买入信号检测 ==========
        detectDoubleGoldenCrossWithDeathBelowZeroAndBreakAboveZero(data, idx, dif, dea, code, signals, BigDecimal.ONE);
        detectGoldenCrossAndBreakAboveZero(data, idx, dif, dea, code, signals, new BigDecimal("0.5"));

        // ========== 卖出信号检测 ==========
        detectConsecutiveDeathCrossAboveZero(data, idx, dif, dea, code, signals, new BigDecimal("0.5"));
        detectDeathCrossAndBreakBelowZero(data, idx, dif, dea, code, signals, BigDecimal.ONE);

        return signals;
    }


    @Override
    public String getDetectorName() {
        if (detectorName == null) {
            detectorName = CharSequenceUtil.format("({},{},{}){}", fast, slow, signal, this.getClass()
                    .getAnnotation(Detector.class)
                    .name());
        }
        return detectorName;
    }

}