package com.xiw.kuwei.chart;

import com.xiw.kuwei.chart.render.ChineseCandlestickRenderer;
import com.xiw.kuwei.chart.render.MacdBarRenderer;
import com.xiw.kuwei.chart.render.VolumeBarRenderer;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import com.xiw.kuwei.vo.stock.StockInfoVO;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.*;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

public class ThsTradeIndexChart {

    public static JFreeChart createChart(StockInfoVO stockInfoVO, String title) {
        List<StockDailyInfoVO> list = stockInfoVO.getStockDailyInfoVOList();

        SymbolAxis xAxis = createTradeAxis(list);

        // ================= K线 =================
        DefaultHighLowDataset ohlc = buildKlineDataset(list);
        NumberAxis priceAxis = createPriceAxis(list);

        ChineseCandlestickRenderer renderer = new ChineseCandlestickRenderer();
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

        XYBarDataset barDataset = new XYBarDataset(new XYSeriesCollection(macd), 0.2);

        XYSeriesCollection lineDs = new XYSeriesCollection();
        lineDs.addSeries(dif);
        lineDs.addSeries(dea);

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

        XYPlot plot = new XYPlot();
        plot.setRangeAxis(axis);

        plot.setDataset(0, barDataset);
        plot.setRenderer(0, barRenderer);

        plot.setDataset(1, lineDs);
        plot.setRenderer(1, lineRenderer);

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

            BigDecimal val = list.get(i).getMaInfo().get(n);

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

    static void applyDarkTheme(XYPlot plot) {
        plot.setBackgroundPaint(Color.BLACK);
        plot.setRangeGridlinePaint(new Color(60, 60, 60));
        plot.setDomainGridlinePaint(new Color(60, 60, 60));
    }

}