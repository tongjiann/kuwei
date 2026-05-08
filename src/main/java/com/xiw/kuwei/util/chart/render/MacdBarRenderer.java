package com.xiw.kuwei.util.chart.render;

import org.jfree.chart.renderer.xy.XYBarRenderer;

import java.awt.*;

/**
 * MACD 柱状图渲染器（红涨绿跌）
 */
public class MacdBarRenderer extends XYBarRenderer {

    @Override
    public Paint getItemPaint(int row, int col) {
        double v = getPlot().getDataset(0).getYValue(row, col);
        return v >= 0 ? Color.RED : Color.GREEN;
    }

}