package com.xiw.kuwei.util.chart;

import com.xiw.kuwei.vo.backtest.PortfolioBackTestResult;
import com.xiw.kuwei.vo.backtest.PortfolioDailyRecord;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 回测对比图表生成器
 */
public class BackTestChart {

    private static final Color ASSET_COLOR = new Color(255, 69, 0);      // 橙红

    private static final Color[] BENCHMARK_COLORS = {
            new Color(30, 144, 255),   // 道奇蓝
            new Color(50, 205, 50),    // 酸橙绿
            new Color(255, 215, 0),    // 金黄
            new Color(186, 85, 211),   // 中紫
            new Color(255, 99, 71)     // 番茄红
    };

    // 策略曲线颜色池（可自定义）
    private static final Color[] STRATEGY_COLORS = {
            new Color(255, 69, 0),      // 橙红
            new Color(30, 144, 255),    // 道奇蓝
            new Color(50, 205, 50),     // 酸橙绿
            new Color(255, 215, 0),     // 金黄
            new Color(186, 85, 211),    // 中紫
            new Color(255, 99, 71),     // 番茄红
            new Color(0, 139, 139),     // 深青
            new Color(220, 20, 60)      // 猩红
    };

    /**
     * 创建对比图表
     *
     * @param records      回测记录列表（按时间升序）
     * @param benchmarkMap 基准股票数据，Key: 股票名称/代码，Value: 日线数据列表（需已按日期升序）
     * @param title        图表标题
     * @return JFreeChart 对象
     */
    public static JFreeChart createComparisonChart(List<PortfolioDailyRecord> records,
                                                   Map<String, List<StockDailyInfoVO>> benchmarkMap,
                                                   String title) {
        if (records == null || records.isEmpty()) {
            return ChartFactory.createTimeSeriesChart(title, "日期", "收益率 (%)", null);
        }

        // 1. 提取回测资产曲线（日期 → 总资产）
        Map<LocalDate, BigDecimal> assetMap = new LinkedHashMap<>();
        for (PortfolioDailyRecord record : records) {
            if (record.getDate() != null) {
                LocalDate date = record.getDate();
                assetMap.put(date, record.getTotalAsset());
            }
        }

        // 2. 确定初始资产（第一个非零值）
        BigDecimal initialAsset = null;
        for (PortfolioDailyRecord record : records) {
            if (record.getTotalAsset() != null && record.getTotalAsset().compareTo(BigDecimal.ZERO) > 0) {
                initialAsset = record.getTotalAsset();
                break;
            }
        }
        if (initialAsset == null) {
            initialAsset = BigDecimal.ONE;
        }

        // 3. 构建基准数据日期序列（合并所有日期的并集，确保对齐）
        TreeSet<LocalDate> allDates = new TreeSet<>();
        allDates.addAll(assetMap.keySet());
        for (List<StockDailyInfoVO> benchmarkData : benchmarkMap.values()) {
            for (StockDailyInfoVO vo : benchmarkData) {
                if (vo.getDate() != null) {
                    allDates.add(vo.getDate());
                }
            }
        }

        // 4. 创建 TimeSeriesCollection 数据集
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        // 4.1 添加资产曲线
        TimeSeries assetSeries = new TimeSeries("策略资产");
        BigDecimal lastAsset = initialAsset;
        for (LocalDate date : allDates) {
            BigDecimal asset = assetMap.getOrDefault(date, lastAsset);
            if (asset != null) {
                double returnPct = asset.subtract(initialAsset)
                        .divide(initialAsset, 4, RoundingMode.HALF_EVEN)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                assetSeries.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), returnPct);
                lastAsset = asset;
            }
        }
        dataset.addSeries(assetSeries);

        // 4.2 添加各基准曲线
        for (Map.Entry<String, List<StockDailyInfoVO>> entry : benchmarkMap.entrySet()) {
            String benchName = entry.getKey();
            List<StockDailyInfoVO> benchData = entry.getValue();
            if (benchData.isEmpty()) continue;

            // 转换为日期→收盘价的映射
            Map<LocalDate, BigDecimal> priceMap = new LinkedHashMap<>();
            for (StockDailyInfoVO vo : benchData) {
                if (vo.getDate() != null && vo.getTodayClosePrice() != null) {
                    priceMap.put(vo.getDate(), vo.getTodayClosePrice());
                }
            }

            // 基准初始价格（取第一个有数据的日期价格）
            BigDecimal initialPrice = null;
            for (LocalDate date : allDates) {
                if (priceMap.containsKey(date)) {
                    initialPrice = priceMap.get(date);
                    break;
                }
            }
            if (initialPrice == null || initialPrice.compareTo(BigDecimal.ZERO) == 0) continue;

            TimeSeries benchSeries = new TimeSeries(benchName);
            BigDecimal lastPrice = initialPrice;
            for (LocalDate date : allDates) {
                BigDecimal price = priceMap.getOrDefault(date, lastPrice);
                if (price != null) {
                    double returnPct = price.subtract(initialPrice)
                            .divide(initialPrice, 4, RoundingMode.HALF_EVEN)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    benchSeries.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), returnPct);
                    lastPrice = price;
                }
            }
            dataset.addSeries(benchSeries);
        }

        // 5. 创建图表
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title != null ? title : "策略 vs 基准收益率对比",
                "日期",
                "收益率 (%)",
                dataset,
                true,
                true,
                false
        );

        // 6. 美化图表
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setOutlineVisible(false);

        // 日期轴格式化
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new java.text.SimpleDateFormat("yyyy-MM-dd"));
        dateAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        dateAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        rangeAxis.setAutoRangeIncludesZero(false);

        // 渲染器设置
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        // 资产曲线：粗线，带形状
        renderer.setSeriesPaint(0, ASSET_COLOR);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));

        // 基准曲线
        for (int i = 1; i < dataset.getSeriesCount(); i++) {
            Color color = BENCHMARK_COLORS[(i - 1) % BENCHMARK_COLORS.length];
            renderer.setSeriesPaint(i, color);
            renderer.setSeriesStroke(i, new BasicStroke(1.8f));
            renderer.setSeriesShapesVisible(i, false);
        }

        plot.setRenderer(renderer);

        ThsTradeIndexChart.applyDarkTheme(plot);

        return chart;
    }

    /**
     * 创建多策略对比图表（与基准股票对比）
     *
     * @param portfolioBackTestResultList 策略名称 → 回测记录列表
     * @param benchmarkMap          基准股票数据（名称 → 日线数据）
     * @param title                 图表标题
     * @return JFreeChart 对象
     */
    public static JFreeChart createMultiStrategyComparisonChart(
            List<PortfolioBackTestResult> portfolioBackTestResultList,
            Map<String, List<StockDailyInfoVO>> benchmarkMap,
            String title) {

        // 1. 收集所有日期（策略记录日期 + 基准数据日期）
        TreeSet<LocalDate> allDates = new TreeSet<>();
        Map<String, Map<LocalDate, BigDecimal>> strategyAssetMaps = new LinkedHashMap<>();
        Map<String, BigDecimal> strategyInitialAssets = new LinkedHashMap<>();

        Map<String, List<PortfolioDailyRecord>> map = portfolioBackTestResultList.stream()
                .collect(Collectors.toMap(PortfolioBackTestResult::getStrategy, PortfolioBackTestResult::getPortfolioDailyRecordList));
        for (Map.Entry<String, List<PortfolioDailyRecord>> entry : map.entrySet()) {
            String strategyName = entry.getKey();
            List<PortfolioDailyRecord> records = entry.getValue();
            if (records.isEmpty()) continue;

            Map<LocalDate, BigDecimal> assetMap = new LinkedHashMap<>();
            BigDecimal initialAsset = null;
            for (PortfolioDailyRecord record : records) {
                if (record.getDate() != null) {
                    LocalDate date = record.getDate();
                    allDates.add(date);
                    assetMap.put(date, record.getTotalAsset());
                    if (initialAsset == null && record.getTotalAsset() != null
                            && record.getTotalAsset().compareTo(BigDecimal.ZERO) > 0) {
                        initialAsset = record.getTotalAsset();
                    }
                }
            }
            if (initialAsset == null) initialAsset = BigDecimal.ONE;
            strategyAssetMaps.put(strategyName, assetMap);
            strategyInitialAssets.put(strategyName, initialAsset);
        }

        // 收集基准日期
        for (List<StockDailyInfoVO> benchData : benchmarkMap.values()) {
            for (StockDailyInfoVO vo : benchData) {
                if (vo.getDate() != null) {
                    allDates.add(vo.getDate());
                }
            }
        }

        // 2. 构建数据集
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        // 2.1 添加策略资产曲线
        int colorIdx = 0;
        for (String strategyName : strategyAssetMaps.keySet()) {
            Map<LocalDate, BigDecimal> assetMap = strategyAssetMaps.get(strategyName);
            BigDecimal initialAsset = strategyInitialAssets.get(strategyName);

            TimeSeries series = new TimeSeries(strategyName);
            BigDecimal lastAsset = initialAsset;
            for (LocalDate date : allDates) {
                BigDecimal asset = assetMap.getOrDefault(date, lastAsset);
                if (asset != null) {
                    double returnPct = asset.subtract(initialAsset)
                            .divide(initialAsset, 4, RoundingMode.HALF_EVEN)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    series.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), returnPct);
                    lastAsset = asset;
                }
            }
            dataset.addSeries(series);
        }

        // 2.2 添加基准曲线
        int benchColorIdx = 0;
        for (Map.Entry<String, List<StockDailyInfoVO>> entry : benchmarkMap.entrySet()) {
            String benchName = entry.getKey();
            List<StockDailyInfoVO> benchData = entry.getValue();
            if (benchData.isEmpty()) continue;

            Map<LocalDate, BigDecimal> priceMap = new LinkedHashMap<>();
            for (StockDailyInfoVO vo : benchData) {
                if (vo.getDate() != null && vo.getTodayClosePrice() != null) {
                    priceMap.put(vo.getDate(), vo.getTodayClosePrice());
                }
            }

            BigDecimal initialPrice = null;
            for (LocalDate date : allDates) {
                if (priceMap.containsKey(date)) {
                    initialPrice = priceMap.get(date);
                    break;
                }
            }
            if (initialPrice == null || initialPrice.compareTo(BigDecimal.ZERO) == 0) continue;

            TimeSeries benchSeries = new TimeSeries(benchName);
            BigDecimal lastPrice = initialPrice;
            for (LocalDate date : allDates) {
                BigDecimal price = priceMap.getOrDefault(date, lastPrice);
                if (price != null) {
                    double returnPct = price.subtract(initialPrice)
                            .divide(initialPrice, 4, RoundingMode.HALF_EVEN)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    benchSeries.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), returnPct);
                    lastPrice = price;
                }
            }
            dataset.addSeries(benchSeries);
        }

        // 3. 创建图表
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title != null ? title : "多策略回测对比",
                "日期",
                "收益率 (%)",
                dataset,
                true,
                true,
                false
        );

        // 4. 美化
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setOutlineVisible(false);

        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new java.text.SimpleDateFormat("yyyy-MM-dd"));
        dateAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        dateAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        rangeAxis.setAutoRangeIncludesZero(false);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        int seriesCount = dataset.getSeriesCount();
        int strategyCount = strategyAssetMaps.size();

        for (int i = 0; i < seriesCount; i++) {
            if (i < strategyCount) {
                // 策略曲线：粗线，带形状
                Color color = STRATEGY_COLORS[i % STRATEGY_COLORS.length];
                renderer.setSeriesPaint(i, color);
                renderer.setSeriesStroke(i, new BasicStroke(2.5f));
                renderer.setSeriesShapesVisible(i, true);
                renderer.setSeriesShape(i, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));
            } else {
                // 基准曲线：细虚线或实线，无形状
                Color color = BENCHMARK_COLORS[(i - strategyCount) % BENCHMARK_COLORS.length];
                renderer.setSeriesPaint(i, color);
                renderer.setSeriesStroke(i, new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f, 3f}, 0f));
                renderer.setSeriesShapesVisible(i, false);
            }
        }

        plot.setRenderer(renderer);

        ThsTradeIndexChart.applyDarkTheme(plot);

        return chart;
    }


    /**
     * 快捷方法：显示图表在 JFrame 中
     */
    public static void showChart(JFreeChart chart, String title) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new ChartPanel(chart));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}