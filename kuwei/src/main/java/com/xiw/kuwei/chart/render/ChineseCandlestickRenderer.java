package com.xiw.kuwei.chart.render;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;


/**
 * 自定义 K 线渲染器（中国股市习惯：红涨绿跌）
 */
public class ChineseCandlestickRenderer extends CandlestickRenderer {

    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea,
                         PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis,
                         XYDataset dataset, int series, int item, CrosshairState crosshairState,
                         int pass) {

        OHLCDataset highLow = (OHLCDataset) dataset;

        double open = highLow.getOpenValue(series, item);
        double close = highLow.getCloseValue(series, item);
        double high = highLow.getHighValue(series, item);
        double low = highLow.getLowValue(series, item);

        double x = domainAxis.valueToJava2D(item, dataArea, plot.getDomainAxisEdge());
        double yOpen = rangeAxis.valueToJava2D(open, dataArea, plot.getRangeAxisEdge());
        double yClose = rangeAxis.valueToJava2D(close, dataArea, plot.getRangeAxisEdge());
        double yHigh = rangeAxis.valueToJava2D(high, dataArea, plot.getRangeAxisEdge());
        double yLow = rangeAxis.valueToJava2D(low, dataArea, plot.getRangeAxisEdge());

        double barWidth = 10;

        // ------------------ 颜色 ------------------
        boolean up = close >= open;

        if (up) {
            g2.setPaint(Color.RED); // 红色边框
            g2.setStroke(new BasicStroke(1.5f));
            // 画空心矩形
            Rectangle2D rect = new Rectangle2D.Double(x - barWidth / 2, Math.min(yOpen, yClose),
                    barWidth, Math.abs(yClose - yOpen));
            g2.draw(rect);
        } else {
            g2.setPaint(Color.GREEN); // 绿色实心
            Rectangle2D rect = new Rectangle2D.Double(x - barWidth / 2, Math.min(yOpen, yClose),
                    barWidth, Math.abs(yClose - yOpen));
            g2.fill(rect);
        }

        // ------------------ 上下影线 ------------------
        g2.setPaint(Color.WHITE);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new Line2D.Double(x, yHigh, x, Math.min(yOpen, yClose)));
        g2.draw(new Line2D.Double(x, yLow, x, Math.max(yOpen, yClose)));
    }

}
