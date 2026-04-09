package com.xiw.kuwei.calculator;

import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import com.xiw.kuwei.vo.stock.StockInfoVO;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.*;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ThsTradeIndexChart {

    public static JFreeChart createChart(StockInfoVO stockInfoVO, String title) {
        List<StockDailyInfoVO> list = stockInfoVO.getStockDailyInfoVOList();

        SymbolAxis xAxis = createTradeAxis(list);

        // ================= K线 =================
        DefaultHighLowDataset ohlc = buildKlineDataset(list);
        NumberAxis priceAxis = createPriceAxis(list);

        MyCandlestickRenderer renderer = new MyCandlestickRenderer();
        renderer.setUpPaint(Color.RED);
        renderer.setDownPaint(Color.GREEN);

        XYPlot klinePlot = new XYPlot(ohlc, null, priceAxis, renderer);
        applyDarkTheme(klinePlot);

        // 添加均线
        addMaLine(klinePlot, list, 5, Color.YELLOW, 1);
        addMaLine(klinePlot, list, 10, Color.CYAN, 2);
        addMaLine(klinePlot, list, 20, Color.MAGENTA, 3);
        addMaLine(klinePlot, list, 30, Color.ORANGE, 4);

        // ================= 交易量 =================
        NumberAxis volumeAxis = new NumberAxis("成交量");
        volumeAxis.setAutoRangeIncludesZero(true);
        volumeAxis.setLabelPaint(Color.WHITE);
        volumeAxis.setTickLabelPaint(Color.WHITE);

        XYBarDataset volumeDataset = new XYBarDataset(buildVolumeDataset(list), 0.8);
        VolumeBarRenderer volumeRenderer = new VolumeBarRenderer(list); // 使用自定义渲染器
        volumeRenderer.setBarPainter(new StandardXYBarPainter());

        XYPlot volumePlot = new XYPlot(volumeDataset, null, volumeAxis, volumeRenderer);
        applyDarkTheme(volumePlot);

        // ================= MACD =================
        XYPlot macdPlot = createMacdPlot(list);

        // ================= 合并 =================
        CombinedDomainXYPlot combined = new CombinedDomainXYPlot(xAxis);
        combined.add(klinePlot, 3);
        combined.add(volumePlot, 1);   // 交易量权重
        combined.add(macdPlot, 1);     // MACD权重

        JFreeChart chart = new JFreeChart(
                title,                     // 主标题
                new Font("微软雅黑", Font.BOLD, 50),
                combined,                  // CombinedDomainXYPlot
                true                       // 显示图例
        );

        chart.getTitle().setPaint(Color.WHITE);

        // 设置暗黑背景
        chart.setBackgroundPaint(Color.BLACK);

        // 添加副标题
        TextTitle subTitle = new TextTitle("MACD + K线 + 成交量", new Font("微软雅黑", Font.PLAIN, 12));
        subTitle.setPaint(Color.LIGHT_GRAY);
        chart.addSubtitle(subTitle);

        return chart;
    }

    // ================= 生成交易量数据集 =================
    private static XYDataset buildVolumeDataset(List<StockDailyInfoVO> list) {
        XYSeries series = new XYSeries("Volume");
        for (int i = 0; i < list.size(); i++) {
            StockDailyInfoVO d = list.get(i);
            series.add(i, d.getTurnover().doubleValue());
        }
        return new XYSeriesCollection(series);
    }
    // ================= MACD + 背离 =================
    private static XYPlot createMacdPlot(List<StockDailyInfoVO> list) {

        XYSeries dif = new XYSeries("DIF");
        XYSeries dea = new XYSeries("DEA");
        XYSeries macd = new XYSeries("MACD");

        XYSeries topDiv = new XYSeries("TopDiv");     // 顶背离
        XYSeries bottomDiv = new XYSeries("BottomDiv"); // 底背离

        for (int i = 0; i < list.size(); i++) {

            StockDailyInfoVO d = list.get(i);
            if (d.getMacdInfo() == null) continue;

            double difVal = d.getMacdInfo().getDif().doubleValue();
            double deaVal = d.getMacdInfo().getDea().doubleValue();
            double macdVal = d.getMacdInfo().getMacd().doubleValue();

            dif.add(i, difVal);
            dea.add(i, deaVal);
            macd.add(i, macdVal);
        }

        // ================= 背离识别 =================
        for (int i = 2; i < list.size(); i++) {

            StockDailyInfoVO p1 = list.get(i - 2);
            StockDailyInfoVO p2 = list.get(i);

            if (p1.getMacdInfo() == null || p2.getMacdInfo() == null) continue;

            double price1 = p1.getHighPrice().doubleValue();
            double price2 = p2.getHighPrice().doubleValue();

            double dif1 = p1.getMacdInfo().getDif().doubleValue();
            double dif2 = p2.getMacdInfo().getDif().doubleValue();

            // 🔴 顶背离
            if (price2 > price1 && dif2 < dif1) {
                topDiv.add(i, dif2);
            }

            // 🟢 底背离
            double low1 = p1.getLowPrice().doubleValue();
            double low2 = p2.getLowPrice().doubleValue();

            if (low2 < low1 && dif2 > dif1) {
                bottomDiv.add(i, dif2);
            }
        }

        XYBarDataset barDataset = new XYBarDataset(new XYSeriesCollection(macd), 0.2);

        XYSeriesCollection lineDs = new XYSeriesCollection();
        lineDs.addSeries(dif);
        lineDs.addSeries(dea);

        XYSeriesCollection divDs = new XYSeriesCollection();
        divDs.addSeries(topDiv);
        divDs.addSeries(bottomDiv);

        NumberAxis axis = new NumberAxis("MACD");
        axis.setLabelPaint(Color.WHITE);
        axis.setTickLabelPaint(Color.WHITE);
        axis.setAutoRangeIncludesZero(true);

        // ===== Renderer =====
        MacdBarRenderer barRenderer = new MacdBarRenderer();
        barRenderer.setShadowVisible(false);
        barRenderer.setBarPainter(new StandardXYBarPainter());

        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
        lineRenderer.setSeriesPaint(0, new Color( 87,100,246));
        lineRenderer.setSeriesPaint(1 , new Color(219, 93,238));

        XYLineAndShapeRenderer divRenderer = new XYLineAndShapeRenderer(false, true);

        // 🔴 顶背离
        divRenderer.setSeriesPaint(0, Color.RED);
        divRenderer.setSeriesShape(0, ShapeUtils.createDownTriangle(8));

        // 🟢 底背离
        divRenderer.setSeriesPaint(1, Color.GREEN);
        divRenderer.setSeriesShape(1, ShapeUtils.createUpTriangle(8));

        XYPlot plot = new XYPlot();
        plot.setRangeAxis(axis);

        plot.setDataset(0, barDataset);
        plot.setRenderer(0, barRenderer);

        plot.setDataset(1, lineDs);
        plot.setRenderer(1, lineRenderer);

        plot.setDataset(2, divDs);
        plot.setRenderer(2, divRenderer);

        // 👉 0轴
        ValueMarker zero = new ValueMarker(0);
        zero.setPaint(Color.GRAY);
        plot.addRangeMarker(zero);

        applyDarkTheme(plot);

        return plot;
    }

    // ================= K线 =================
    private static DefaultHighLowDataset buildKlineDataset(List<StockDailyInfoVO> list) {

        int size = list.size();

        Date[] dates = new Date[size];
        double[] highs = new double[size];
        double[] lows = new double[size];
        double[] opens = new double[size];
        double[] closes = new double[size];
        double[] volumes = new double[size];

        for (int i = 0; i < size; i++) {

            StockDailyInfoVO d = list.get(i);

            dates[i] = new Date(i);
            opens[i] = d.getOpenPrice().doubleValue();
            highs[i] = d.getHighPrice().doubleValue();
            lows[i] = d.getLowPrice().doubleValue();
            closes[i] = d.getTodayClosePrice().doubleValue();
            volumes[i] = 0;
        }

        return new DefaultHighLowDataset("KLine", dates, highs, lows, opens, closes, volumes);
    }


    private static void addMaLine(XYPlot plot, List<StockDailyInfoVO> list,
                                  int n, Color color, int index) {

        XYSeries s = new XYSeries("MA" + n);

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).getMaInfo() == null) continue;

            BigDecimal val = switch (n) {
                case 5 -> list.get(i).getMaInfo().getMa5();
                case 10 -> list.get(i).getMaInfo().getMa10();
                case 20 -> list.get(i).getMaInfo().getMa20();
                case 30 -> list.get(i).getMaInfo().getMa30();
                default -> null;
            };

            if (val != null) {
                s.add(i, val.doubleValue());
            }
        }

        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer(true, false);
        r.setSeriesPaint(0, color);

        plot.setDataset(index, new XYSeriesCollection(s));
        plot.setRenderer(index, r);
    }

    private static NumberAxis createPriceAxis(List<StockDailyInfoVO> list) {

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (StockDailyInfoVO d : list) {
            min = Math.min(min, d.getLowPrice().doubleValue());
            max = Math.max(max, d.getHighPrice().doubleValue());
        }

        NumberAxis axis = new NumberAxis("单位价格");
        axis.setRange(min * 0.95, max * 1.05);
        axis.setLabelPaint(Color.WHITE);
        axis.setTickLabelPaint(Color.WHITE);
        axis.setNumberFormatOverride(new DecimalFormat("#0.00"));

        return axis;
    }

    private static SymbolAxis createTradeAxis(List<StockDailyInfoVO> list) {

        int size = list.size();
        String[] labels = new String[size];

        for (int i = 0; i < size; i++) {
            labels[i] = list.get(i).getDate().toString();
        }

        SymbolAxis axis = new SymbolAxis("日期", labels);
        axis.setTickLabelPaint(Color.WHITE);
        axis.setLabelPaint(Color.WHITE);

        return axis;
    }

    private static void applyDarkTheme(XYPlot plot) {
        plot.setBackgroundPaint(Color.BLACK);
        plot.setRangeGridlinePaint(new Color(60, 60, 60));
        plot.setDomainGridlinePaint(new Color(60, 60, 60));
    }

    // ================= mock =================
    private static List<StockDailyInfoVO> mockData() {

        List<StockDailyInfoVO> list = new ArrayList<>();
        Random r = new Random();
        double price = 100;

        for (int i = 0; i < 80; i++) {

            StockDailyInfoVO vo = new StockDailyInfoVO();
            vo.setDate(java.time.LocalDate.now().minusDays(80 - i));

            double open = price + r.nextDouble() * 4 - 2;
            double close = open + r.nextDouble() * 4 - 2;
            double high = Math.max(open, close) + r.nextDouble();
            double low = Math.min(open, close) - r.nextDouble();

            vo.setOpenPrice(BigDecimal.valueOf(open));
            vo.setTodayClosePrice(BigDecimal.valueOf(close));
            vo.setHighPrice(BigDecimal.valueOf(high));
            vo.setLowPrice(BigDecimal.valueOf(low));

            price = close;
            list.add(vo);
        }
        return list;
    }

    // ================= Renderer =================
    static class MacdBarRenderer extends XYBarRenderer {

        @Override
        public Paint getItemPaint(int row, int col) {
            double v = getPlot().getDataset(0).getYValue(row, col);
            return v >= 0 ? Color.RED : Color.GREEN;
        }

    }

    static class MyCandlestickRenderer extends CandlestickRenderer {

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

    // ================= 交易量渲染器（涨空心、跌实心） =================
    static class VolumeBarRenderer extends XYBarRenderer {

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

}