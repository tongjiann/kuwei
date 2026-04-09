package com.xiw.kuwei.chart.render;

import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;

import java.awt.*;
import java.util.List;


// ================= 交易量渲染器（涨空心、跌实心） =================
public class VolumeBarRenderer extends XYBarRenderer {

    private final List<StockDailyInfoVO> list;

    public VolumeBarRenderer(List<StockDailyInfoVO> list) {
        super();
        this.list = list;
        setShadowVisible(false);
        setBarPainter(new StandardXYBarPainter());
        setDrawBarOutline(true); // 开启边框
        setSeriesOutlinePaint(0, Color.RED); // 默认边框颜色
        setSeriesOutlineStroke(0, new BasicStroke(1.5f));
    }

    @Override
    public Paint getItemPaint(int row, int col) {
        if (col >= list.size()) return Color.GRAY;
        StockDailyInfoVO d = list.get(col);
        double diff = d.getTodayClosePrice().doubleValue() - d.getOpenPrice().doubleValue();

        if (diff >= 0) {
            // 涨 → 空心柱，只画边框
            setSeriesOutlinePaint(0, Color.RED);
            return new Color(0, 0, 0, 0); // 透明填充
        } else {
            // 跌 → 实心柱
            setSeriesOutlinePaint(0, Color.GREEN);
            return Color.GREEN;
        }
    }

}
