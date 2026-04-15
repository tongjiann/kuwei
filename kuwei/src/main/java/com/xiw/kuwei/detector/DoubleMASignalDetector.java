package com.xiw.kuwei.detector;

import com.xiw.kuwei.annotation.Detector;
import com.xiw.kuwei.exception.LogicalException;
import com.xiw.kuwei.vo.backtest.Signal;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * MAquick + MAslow 双均线交易信号识别器
 * <p>
 * 规则：
 * 1. MAquick 上穿 MAslow → 买入
 * 2. MAquick 下穿 MAslow → 卖出
 */
@Detector(name = "双均线交易信号识别器")
public class DoubleMASignalDetector implements DetectorInterface {

    private final BigDecimal buyStrength;

    private final BigDecimal sellStrength;

    private final int maPeriodSlow;

    private final int maPeriodQuick;

    private String detectorName;

    public DoubleMASignalDetector(Integer maPeriodQuick, Integer maPeriodSlow, BigDecimal buyStrength, BigDecimal sellStrength) {
        if (maPeriodQuick <= 0 || maPeriodSlow <= 0 || maPeriodQuick.equals(maPeriodSlow)) {
            throw new LogicalException("参数不合法+" + maPeriodQuick + "|" + maPeriodSlow);
        }
        this.buyStrength = buyStrength;
        this.sellStrength = sellStrength;
        this.maPeriodSlow = maPeriodSlow;
        this.maPeriodQuick = maPeriodQuick;
    }

    private void ensureMaCalculated(List<StockDailyInfoVO> data) {
        for (int i = 0; i < data.size(); i++) {
            StockDailyInfoVO vo = data.get(i);

            if (vo.getMaInfo() == null) {
                vo.setMaInfo(new HashMap<>());
            }

            // ==== MA Quick ====
            if (vo.getMaInfo().get(maPeriodQuick) == null && i >= maPeriodQuick - 1) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - maPeriodQuick + 1; j <= i; j++) {
                    sum = sum.add(data.get(j).getTodayClosePrice());
                }
                vo.getMaInfo().put(maPeriodQuick,
                        sum.divide(BigDecimal.valueOf(maPeriodQuick), 4, RoundingMode.HALF_EVEN)
                );
            }

            // ==== MA Slow ====
            if (vo.getMaInfo().get(maPeriodSlow) == null && i >= maPeriodSlow - 1) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - maPeriodSlow + 1; j <= i; j++) {
                    sum = sum.add(data.get(j).getTodayClosePrice());
                }
                vo.getMaInfo().put(maPeriodSlow,
                        sum.divide(BigDecimal.valueOf(maPeriodSlow), 4, RoundingMode.HALF_EVEN)
                );
            }
        }
    }

    @Override
    public List<Signal> doDetectSignals(List<StockDailyInfoVO> data, String code) {
        List<Signal> signals = new ArrayList<>();

        if (data == null || data.size() < maPeriodQuick + 1) return signals;

        ensureMaCalculated(data);

        for (int i = 1; i < data.size(); i++) {
            StockDailyInfoVO prev = data.get(i - 1);
            StockDailyInfoVO curr = data.get(i);

            BigDecimal prevMaQuick = prev.getMaInfo().get(maPeriodQuick);
            BigDecimal prevMaSlow = prev.getMaInfo().get(maPeriodSlow);
            BigDecimal currMaQuick = curr.getMaInfo().get(maPeriodQuick);
            BigDecimal currMaSlow = curr.getMaInfo().get(maPeriodSlow);
            BigDecimal currClose = curr.getTodayClosePrice();

            if (prevMaQuick == null || prevMaSlow == null ||
                    currMaQuick == null || currMaSlow == null ||
                    currClose == null) {
                continue;
            }

            // 金叉：快线上穿慢线
            boolean goldenCross = prevMaQuick.compareTo(prevMaSlow) <= 0 &&
                    currMaQuick.compareTo(currMaSlow) > 0;

            // 死叉：快线下穿慢线
            boolean deathCross = prevMaQuick.compareTo(prevMaSlow) >= 0 &&
                    currMaQuick.compareTo(currMaSlow) < 0;

            if (goldenCross && currClose.compareTo(currMaQuick) > 0) {
                signals.add(buildSignal(0, code, curr, buyStrength,
                        "MA" + maPeriodQuick + "上穿MA" + maPeriodSlow + "（金叉）"));
            } else if (deathCross) {
                signals.add(buildSignal(1, code, curr, sellStrength,
                        "MA" + maPeriodQuick + "下穿MA" + maPeriodSlow + "（死叉）"));
            }
        }

        return signals;
    }

    @Override
    public String getDetectorName() {
        if (detectorName == null) {
            detectorName = "MA" + maPeriodQuick + "&MA" + maPeriodSlow +
                    this.getClass().getAnnotation(Detector.class).name();
        }
        return detectorName;
    }

}