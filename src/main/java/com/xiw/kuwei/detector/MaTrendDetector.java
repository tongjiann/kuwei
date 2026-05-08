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
 * 日均线趋势交易信号识别器
 * 规则：今日MA > 昨日MA → 买入
 * 今日MA < 昨日MA → 卖出
 */
@Detector(name = "日均线趋势交易信号识别器")
public class MaTrendDetector implements DetectorInterface {

    private final int maPeriod;

    private final BigDecimal buyStrength;

    private final BigDecimal sellStrength;

    private String detectorName;

    public MaTrendDetector(Integer maPeriod) {
        this.maPeriod = maPeriod;
        this.buyStrength = BigDecimal.ONE;
        this.sellStrength = BigDecimal.ONE;
    }

    public MaTrendDetector(Integer maPeriod, BigDecimal strength) {
        this.maPeriod = maPeriod;
        this.buyStrength = strength;
        this.sellStrength = strength;
    }

    public MaTrendDetector(Integer maPeriod, BigDecimal buyStrength, BigDecimal sellStrength) {
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

    @Override
    public List<Signal> doDetectSignals(List<StockDailyInfoVO> data, String code) {
        List<Signal> signals = new ArrayList<>();

        if (data == null || data.size() < maPeriod + 1) {
            return signals; // 数据不足，无法判断趋势
        }

        // 1. 确保 MA 已计算
        ensureMaCalculated(data);

        // 2. 从第二根K线开始比较均线趋势（需要前一日MA）
        for (int i = 1; i < data.size(); i++) {
            StockDailyInfoVO prev = data.get(i - 1);
            StockDailyInfoVO curr = data.get(i);

            BigDecimal prevMa = (prev.getMaInfo() != null) ? prev.getMaInfo().get(maPeriod) : null;
            BigDecimal currMa = (curr.getMaInfo() != null) ? curr.getMaInfo().get(maPeriod) : null;

            if (prevMa == null || currMa == null) {
                continue;
            }

            // 均线上升 → 买入信号
            if (currMa.compareTo(prevMa) > 0) {
                signals.add(buildSignal(0, code, curr, buyStrength, maPeriod + "日均线上升（买入）"));
            }
            // 均线下降 → 卖出信号
            else if (currMa.compareTo(prevMa) < 0) {
                signals.add(buildSignal(1, code, curr, sellStrength, maPeriod + "日均线下降（卖出）"));
            }
            // 持平则不产生信号
        }

        return signals;
    }

    @Override
    public String getDetectorName() {
        if (detectorName == null) {
            detectorName = "MA" + maPeriod + "买强度:" + buyStrength + "卖强度:" + sellStrength + this.getClass()
                    .getAnnotation(Detector.class)
                    .name();
        }
        return detectorName;
    }

}