package com.xiw.kuwei.detector;

import com.xiw.kuwei.annotation.Detector;
import com.xiw.kuwei.vo.backtest.Signal;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 日均线交易信号识别器
 * 规则：收盘价上穿日均线 → 买入（强度=1）
 * 收盘价下穿日均线 → 卖出（强度=1）
 */
@Detector(name = "日均线交易信号识别器")
public class MaSignalDetector implements DetectorInterface {

    private final int maPeriod;

    private BigDecimal buyStrength = BigDecimal.ONE;

    private BigDecimal sellStrength = BigDecimal.ONE;

    private String detectorName = null;

    public MaSignalDetector(int maPeriod) {
        this.maPeriod = maPeriod;
    }

    public MaSignalDetector(Integer maPeriod, BigDecimal buyStrength, BigDecimal sellStrength) {
        this.maPeriod = maPeriod;
        this.buyStrength = buyStrength;
        this.sellStrength = sellStrength;
    }

    /**
     * 确保数据中 MA 已计算，若未计算则动态计算并填充
     */
    private void ensureMaCalculated(List<StockDailyInfoVO> data) {
        for (int i = 0; i < data.size(); i++) {
            StockDailyInfoVO vo = data.get(i);
            if (vo.getMaInfo() == null) {
                vo.setMaInfo(new HashMap<>());
            }
            if (vo.getMaInfo().get(maPeriod) != null) {
                continue; // 已存在，跳过
            }

            if (i >= maPeriod - 1) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - maPeriod + 1; j <= i; j++) {
                    sum = sum.add(data.get(j).getTodayClosePrice());
                }
                BigDecimal ma = sum.divide(BigDecimal.valueOf(maPeriod), 4, RoundingMode.HALF_EVEN);
                vo.getMaInfo().put(maPeriod, ma);
            }
        }
    }

    /**
     * 检测所有信号
     *
     * @param data 包含均线信息的日线数据（需已计算 MA）
     * @param code 股票代码
     * @return 信号列表
     */
    public List<Signal> doDetectSignals(List<StockDailyInfoVO> data, String code) {
        List<Signal> signals = new ArrayList<>();

        if (data == null || data.size() < maPeriod + 1) return signals;

        // 1. 确保数据中 MA 已计算，若未计算则动态计算
        ensureMaCalculated(data);

        // 2. 遍历寻找穿越信号
        for (int i = 1; i < data.size(); i++) {
            StockDailyInfoVO prev = data.get(i - 1);
            StockDailyInfoVO curr = data.get(i);

            BigDecimal prevClose = prev.getTodayClosePrice();
            BigDecimal currClose = curr.getTodayClosePrice();
            BigDecimal prevMa = prev.getMaInfo() != null ? prev.getMaInfo().get(maPeriod) : null;
            BigDecimal currMa = curr.getMaInfo() != null ? curr.getMaInfo().get(maPeriod) : null;

            if (prevClose == null || currClose == null || prevMa == null || currMa == null) {
                continue;
            }

            // 上穿：前一日收盘 <= 前一日MA 且 当日收盘 > 当日MA
            boolean crossAbove = prevClose.compareTo(prevMa) <= 0 && currClose.compareTo(currMa) > 0;

            // 下穿：前一日收盘 >= 前一日MA 且 当日收盘 < 当日MA
            boolean crossBelow = prevClose.compareTo(prevMa) >= 0 && currClose.compareTo(currMa) < 0;

            if (crossAbove) {
                signals.add(buildSignal(0, code, curr, buyStrength, "收盘上穿" + maPeriod + "日均线"));
            } else if (crossBelow) {
                signals.add(buildSignal(1, code, curr, sellStrength, "收盘下穿" + maPeriod + "日均线"));
            }
        }
        return signals;
    }

    @Override
    public String getDetectorName() {
        if (detectorName == null) {
            detectorName = maPeriod + "买强度:" + buyStrength + "卖强度:" + sellStrength + this.getClass()
                    .getAnnotation(Detector.class)
                    .name();
        }
        return detectorName;
    }

}