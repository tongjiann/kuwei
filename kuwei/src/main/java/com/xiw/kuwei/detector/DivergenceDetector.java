package com.xiw.kuwei.detector;

import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 背离识别器
 */
public class DivergenceDetector {

    private static final int WINDOW_SIZE = 3;      // 滑动窗口大小

    private static final int MAX_GAP = 30;          // 两个极值点之间最大允许间隔

    /**
     * 识别顶背离：价格创新高，DIF 未创新高（价格 ↑，DIF ↓）
     */
    public static List<DivergenceResult> detectTopDivergence(
            List<StockDailyInfoVO> data,
            List<PeakTroughDetector.ExtremePoint> pricePeaks,
            List<PeakTroughDetector.ExtremePoint> difPeaks) {

        List<DivergenceResult> results = new ArrayList<>();

        for (int i = 0; i < pricePeaks.size() - 1; i++) {
            PeakTroughDetector.ExtremePoint p1 = pricePeaks.get(i);
            PeakTroughDetector.ExtremePoint p2 = pricePeaks.get(i + 1);

            // 价格必须创新高
            if (p2.value <= p1.value) continue;

            // 找对应的 DIF 波峰
            PeakTroughDetector.ExtremePoint d1 = findMatchingExtreme(p1.index, difPeaks, MAX_GAP);
            PeakTroughDetector.ExtremePoint d2 = findMatchingExtreme(p2.index, difPeaks, MAX_GAP);

            if (d1 == null || d2 == null) continue;

            // DIF 未创新高（顶背离核心条件）
            if (d2.value < d1.value) {
                // 从 data 中获取日期
                LocalDate startDate = data.get(p1.index).getDate();
                LocalDate endDate = data.get(p2.index).getDate();
                results.add(new DivergenceResult(
                        DivergenceResult.Type.TOP,
                        p1.index, p2.index,
                        d1.index, d2.index,
                        p1.value, p2.value,
                        d1.value, d2.value, startDate, endDate
                ));
            }
        }
        return results;
    }

    /**
     * 识别底背离：价格创新低，DIF 未创新低（价格 ↓，DIF ↑）
     */
    public static List<DivergenceResult> detectBottomDivergence(
            List<StockDailyInfoVO> data,
            List<PeakTroughDetector.ExtremePoint> priceTroughs,
            List<PeakTroughDetector.ExtremePoint> difTroughs) {

        List<DivergenceResult> results = new ArrayList<>();

        for (int i = 0; i < priceTroughs.size() - 1; i++) {
            PeakTroughDetector.ExtremePoint p1 = priceTroughs.get(i);
            PeakTroughDetector.ExtremePoint p2 = priceTroughs.get(i + 1);

            // 价格必须创新低
            if (p2.value >= p1.value) continue;

            PeakTroughDetector.ExtremePoint d1 = findMatchingExtreme(p1.index, difTroughs, MAX_GAP);
            PeakTroughDetector.ExtremePoint d2 = findMatchingExtreme(p2.index, difTroughs, MAX_GAP);

            if (d1 == null || d2 == null) continue;

            // DIF 未创新低（底背离核心条件）
            if (d2.value > d1.value) {
                // 从 data 中获取日期
                LocalDate startDate = data.get(p1.index).getDate();
                LocalDate endDate = data.get(p2.index).getDate();
                results.add(new DivergenceResult(
                        DivergenceResult.Type.BOTTOM,
                        p1.index, p2.index,
                        d1.index, d2.index,
                        p1.value, p2.value,
                        d1.value, d2.value, startDate, endDate
                ));
            }
        }
        return results;
    }

    /**
     * 根据价格极值点索引，查找最近的 DIF 极值点
     */
    private static PeakTroughDetector.ExtremePoint findMatchingExtreme(
            int targetIndex, List<PeakTroughDetector.ExtremePoint> extremes, int maxGap) {
        PeakTroughDetector.ExtremePoint best = null;
        int minDist = Integer.MAX_VALUE;

        for (PeakTroughDetector.ExtremePoint e : extremes) {
            int dist = Math.abs(e.index - targetIndex);
            if (dist < maxGap && dist < minDist) {
                minDist = dist;
                best = e;
            }
        }
        return best;
    }

    /**
     * 从数据中提取价格序列（最高价用于顶背离，最低价用于底背离）
     */
    public static double[] extractHighPrices(List<StockDailyInfoVO> data) {
        return data.stream().mapToDouble(v -> v.getHighPrice().doubleValue()).toArray();
    }

    public static double[] extractLowPrices(List<StockDailyInfoVO> data) {
        return data.stream().mapToDouble(v -> v.getLowPrice().doubleValue()).toArray();
    }

    public static double[] extractDifValues(List<StockDailyInfoVO> data) {
        return data.stream()
                .filter(v -> v.getMacdInfo() != null)
                .mapToDouble(v -> v.getMacdInfo().getDif().doubleValue())
                .toArray();
    }

    /**
     * 一站式背离识别
     */
    public static DivergenceDetectionResult detectAll(List<StockDailyInfoVO> data) {
        double[] highPrices = extractHighPrices(data);
        double[] lowPrices = extractLowPrices(data);
        double[] difValues = extractDifValues(data);

        List<PeakTroughDetector.ExtremePoint> pricePeaks = PeakTroughDetector.detectPeaks(highPrices, WINDOW_SIZE);
        List<PeakTroughDetector.ExtremePoint> priceTroughs = PeakTroughDetector.detectTroughs(lowPrices, WINDOW_SIZE);
        List<PeakTroughDetector.ExtremePoint> difPeaks = PeakTroughDetector.detectPeaks(difValues, WINDOW_SIZE);
        List<PeakTroughDetector.ExtremePoint> difTroughs = PeakTroughDetector.detectTroughs(difValues, WINDOW_SIZE);

        return new DivergenceDetectionResult(
                detectTopDivergence(data, pricePeaks, difPeaks),
                detectBottomDivergence(data, priceTroughs, difTroughs)
        );
    }

    public static class DivergenceDetectionResult {

        public final List<DivergenceResult> topDivergences;

        public final List<DivergenceResult> bottomDivergences;

        public DivergenceDetectionResult(List<DivergenceResult> top, List<DivergenceResult> bottom) {
            this.topDivergences = top;
            this.bottomDivergences = bottom;
        }

    }

}