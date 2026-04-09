package com.xiw.kuwei.detector;

import java.util.ArrayList;
import java.util.List;

/**
 * 波峰/波谷识别器（滑动窗口法）
 */
public class PeakTroughDetector {

    /**
     * 极点类型
     */
    public enum ExtremeType {
        PEAK,   // 波峰（局部极大值）
        TROUGH  // 波谷（局部极小值）
    }

    /**
     * 极点记录
     */
    public static class ExtremePoint {
        public final int index;
        public final double value;
        public final ExtremeType type;

        public ExtremePoint(int index, double value, ExtremeType type) {
            this.index = index;
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("[%d] %.4f (%s)", index, value, type);
        }
    }

    /**
     * 识别波峰（滑动窗口法）
     * @param data 数据序列
     * @param windowSize 滑动窗口大小（左右各检查 windowSize 个点）
     * @return 波峰列表
     */
    public static List<ExtremePoint> detectPeaks(double[] data, int windowSize) {
        List<ExtremePoint> peaks = new ArrayList<>();
        if (data == null || data.length < windowSize * 2 + 1) return peaks;

        for (int i = windowSize; i < data.length - windowSize; i++) {
            double current = data[i];
            boolean isPeak = true;

            // 检查左右邻域
            for (int j = i - windowSize; j <= i + windowSize; j++) {
                if (j == i) continue;
                if (data[j] >= current) {
                    isPeak = false;
                    break;
                }
            }

            if (isPeak) {
                peaks.add(new ExtremePoint(i, current, ExtremeType.PEAK));
            }
        }

        // 合并相邻波峰（取最大值）
        return mergeAdjacentExtremes(peaks, true);
    }

    /**
     * 识别波谷（滑动窗口法）
     * @param data 数据序列
     * @param windowSize 滑动窗口大小
     * @return 波谷列表
     */
    public static List<ExtremePoint> detectTroughs(double[] data, int windowSize) {
        List<ExtremePoint> troughs = new ArrayList<>();
        if (data == null || data.length < windowSize * 2 + 1) return troughs;

        for (int i = windowSize; i < data.length - windowSize; i++) {
            double current = data[i];
            boolean isTrough = true;

            for (int j = i - windowSize; j <= i + windowSize; j++) {
                if (j == i) continue;
                if (data[j] <= current) {
                    isTrough = false;
                    break;
                }
            }

            if (isTrough) {
                troughs.add(new ExtremePoint(i, current, ExtremeType.TROUGH));
            }
        }

        return mergeAdjacentExtremes(troughs, false);
    }

    /**
     * 合并距离过近的极值点
     */
    private static List<ExtremePoint> mergeAdjacentExtremes(List<ExtremePoint> points, boolean isPeak) {
        if (points.size() < 2) return points;

        List<ExtremePoint> merged = new ArrayList<>();
        ExtremePoint current = points.get(0);

        for (int i = 1; i < points.size(); i++) {
            ExtremePoint next = points.get(i);
            // 如果两点距离 < 3，取极值更显著的那个
            if (next.index - current.index < 3) {
                if (isPeak && next.value > current.value) {
                    current = next;
                } else if (!isPeak && next.value < current.value) {
                    current = next;
                }
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }
}