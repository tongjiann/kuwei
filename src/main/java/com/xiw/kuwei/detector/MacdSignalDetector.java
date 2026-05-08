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

    private final BigDecimal buyStrength1;

    private final BigDecimal sellStrength1;

    private final BigDecimal buyStrength2;

    private final BigDecimal sellStrength2;

    private final BigDecimal buyStrength3;

    private final BigDecimal sellStrength3;

    private final MacdCalculator macdCalculator;

    private String detectorName = null;

    public MacdSignalDetector(Integer fast, Integer slow, Integer signal, BigDecimal buyStrength1, BigDecimal sellStrength1, BigDecimal buyStrength2, BigDecimal sellStrength2, BigDecimal buyStrength3, BigDecimal sellStrength3) {
        this.fast = fast;
        this.slow = slow;
        this.signal = signal;
        this.buyStrength1 = buyStrength1;
        this.sellStrength1 = sellStrength1;
        this.buyStrength2 = buyStrength2;
        this.sellStrength2 = sellStrength2;
        this.buyStrength3 = buyStrength3;
        this.sellStrength3 = sellStrength3;
        this.macdCalculator = new MacdCalculator(fast, slow, signal);
    }


    /**
     * 0轴下双金叉夹死叉 + DIF突破0轴，且期间未触及0轴
     */
    private void detectDoubleGoldenCrossWithDeathBelowZeroAndBreakAboveZero(List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea, String code, List<Signal> signals, BigDecimal strength) {
        if (strength.compareTo(ZERO) == 0) {
            return;
        }

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
     * 0轴上双死叉夹金叉 + DIF跌破0轴，且期间未触及0轴
     */
    private void detectDoubleDeathCrossWithGoldenAboveZeroAndBreakBelowZero(
            List<StockDailyInfoVO> data,
            int[] idx,
            BigDecimal[] dif,
            BigDecimal[] dea,
            String code,
            List<Signal> signals,
            BigDecimal strength) {
        if (strength.compareTo(ZERO) == 0) {
            return;
        }
        List<Integer> deathCrossAboveZeroPositions = new ArrayList<>();

        // 找所有 0轴上死叉
        for (int i = 1; i < dif.length; i++) {
            if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0) {
                if (dif[i].compareTo(ZERO) > 0 && dea[i].compareTo(ZERO) > 0) {
                    deathCrossAboveZeroPositions.add(i);
                }
            }
        }

        // 双死叉结构
        for (int k = 0; k < deathCrossAboveZeroPositions.size() - 1; k++) {
            int firstDeath = deathCrossAboveZeroPositions.get(k);
            int secondDeath = deathCrossAboveZeroPositions.get(k + 1);

            // 中间必须有金叉（仍在0轴上）
            boolean hasGoldenCrossAboveZero = false;
            for (int i = firstDeath + 1; i < secondDeath; i++) {
                if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0) {
                    if (dif[i].compareTo(ZERO) > 0 && dea[i].compareTo(ZERO) > 0) {
                        hasGoldenCrossAboveZero = true;
                        break;
                    }
                }
            }
            if (!hasGoldenCrossAboveZero) continue;

            // 找 DIF 跌破 0轴
            for (int j = secondDeath + 1; j < dif.length; j++) {
                if (dif[j - 1].compareTo(ZERO) > 0 && dif[j].compareTo(ZERO) <= 0) {

                    // 检查期间未触及0轴
                    boolean everBelowZero = false;
                    for (int t = firstDeath; t < j; t++) {
                        if (dif[t].compareTo(ZERO) <= 0 || dea[t].compareTo(ZERO) <= 0) {
                            everBelowZero = true;
                            break;
                        }
                    }

                    if (!everBelowZero) {
                        StockDailyInfoVO vo = data.get(idx[j]);
                        String desc = "0轴上双死叉夹金叉+跌破0轴 DIF:"
                                + dif[j].setScale(2, RoundingMode.HALF_EVEN);

                        signals.add(buildSignal(1, code, vo, strength, desc));
                    }
                    break;
                }
            }
        }
    }

    /**
     * 0轴下 双金叉夹死叉（GC -> DC -> GC）
     */
    private void detectDoubleGoldenCrossWithDeathBelowZero(
            List<StockDailyInfoVO> data,
            int[] idx,
            BigDecimal[] dif,
            BigDecimal[] dea,
            String code,
            List<Signal> signals,
            BigDecimal strength) {

        if (strength.compareTo(ZERO) == 0) {
            return;
        }

        // 收集所有交叉点（含类型）
        List<int[]> crosses = new ArrayList<>();
        // int[]{index, type}  type: 1=金叉, -1=死叉

        for (int i = 1; i < dif.length; i++) {
            // 金叉
            if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0) {
                if (dif[i].compareTo(ZERO) < 0 && dea[i].compareTo(ZERO) < 0) {
                    crosses.add(new int[]{i, 1});
                }
            }
            // 死叉
            else if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0) {
                if (dif[i].compareTo(ZERO) < 0 && dea[i].compareTo(ZERO) < 0) {
                    crosses.add(new int[]{i, -1});
                }
            }
        }

        // 滑动窗口匹配：GC -> DC -> GC
        for (int i = 0; i < crosses.size() - 2; i++) {
            int[] a = crosses.get(i);
            int[] b = crosses.get(i + 1);
            int[] c = crosses.get(i + 2);

            if (a[1] == 1 && b[1] == -1 && c[1] == 1) {
                StockDailyInfoVO vo = data.get(idx[c[0]]);
                signals.add(buildSignal(0, code, vo, strength, "0轴下双金叉夹死叉"));
            }
        }
    }

    /**
     * 0轴上 双死叉夹金叉（DC -> GC -> DC）
     */
    private void detectDoubleDeathCrossWithGoldenAboveZero(
            List<StockDailyInfoVO> data,
            int[] idx,
            BigDecimal[] dif,
            BigDecimal[] dea,
            String code,
            List<Signal> signals,
            BigDecimal strength) {

        if (strength.compareTo(ZERO) == 0) {
            return;
        }

        List<int[]> crosses = new ArrayList<>();
        // type: 1=金叉, -1=死叉

        for (int i = 1; i < dif.length; i++) {
            // 金叉
            if (dif[i - 1].compareTo(dea[i - 1]) <= 0 && dif[i].compareTo(dea[i]) > 0) {
                if (dif[i].compareTo(ZERO) > 0 && dea[i].compareTo(ZERO) > 0) {
                    crosses.add(new int[]{i, 1});
                }
            }
            // 死叉
            else if (dif[i - 1].compareTo(dea[i - 1]) >= 0 && dif[i].compareTo(dea[i]) < 0) {
                if (dif[i].compareTo(ZERO) > 0 && dea[i].compareTo(ZERO) > 0) {
                    crosses.add(new int[]{i, -1});
                }
            }
        }

        // 匹配：DC -> GC -> DC
        for (int i = 0; i < crosses.size() - 2; i++) {
            int[] a = crosses.get(i);
            int[] b = crosses.get(i + 1);
            int[] c = crosses.get(i + 2);

            if (a[1] == -1 && b[1] == 1 && c[1] == -1) {
                StockDailyInfoVO vo = data.get(idx[c[0]]);
                signals.add(buildSignal(1, code, vo, strength, "0轴上双死叉夹金叉"));
            }
        }
    }
    /**
     * 死叉后跌破0轴
     */
    private void detectDeathCrossAndBreakBelowZero(List<StockDailyInfoVO> data, int[] idx, BigDecimal[] dif, BigDecimal[] dea, String code, List<Signal> signals, BigDecimal strength) {
        if (strength.compareTo(ZERO) == 0) {
            return;
        }
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
        if (strength.compareTo(ZERO) == 0) {
            return;
        }
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
        detectDoubleGoldenCrossWithDeathBelowZeroAndBreakAboveZero(data, idx, dif, dea, code, signals, buyStrength1);
        detectDoubleGoldenCrossWithDeathBelowZero(data, idx, dif, dea, code, signals, buyStrength2);
        detectGoldenCrossAndBreakAboveZero(data, idx, dif, dea, code, signals, buyStrength3);

        // ========== 卖出信号检测 ==========
        detectDoubleDeathCrossWithGoldenAboveZeroAndBreakBelowZero(data, idx, dif, dea, code, signals, sellStrength1);
        detectDoubleDeathCrossWithGoldenAboveZero(data, idx, dif, dea, code, signals, sellStrength2);
        detectDeathCrossAndBreakBelowZero(data, idx, dif, dea, code, signals, sellStrength3);

        return signals;
    }


    @Override
    public String getDetectorName() {
        if (detectorName == null) {
            detectorName = CharSequenceUtil.format("({},{},{}|{}-{}|{}-{}|{}-{}){}",
                    fast, slow, signal, buyStrength1, sellStrength1,
                    buyStrength2, sellStrength2, buyStrength3, sellStrength3,
                    this.getClass()
                    .getAnnotation(Detector.class)
                    .name());
        }
        return detectorName;
    }

}