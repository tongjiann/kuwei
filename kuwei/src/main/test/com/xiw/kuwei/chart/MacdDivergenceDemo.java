package com.xiw.kuwei.chart;

import com.xiw.kuwei.calculator.MacdCalculator;
import com.xiw.kuwei.detector.DivergenceDetector;
import com.xiw.kuwei.detector.DivergenceResult;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 完整运行示例
 */
public class MacdDivergenceDemo extends JFrame {

    public MacdDivergenceDemo() {
        super("MACD 背离识别 - 专业版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 1. 生成模拟数据（或从文件/数据库读取）
        List<StockDailyInfoVO> data = generateMockData(200);

        // 2. 计算 MACD 指标
        MacdCalculator.calculate(data);

        // 3. 识别背离
        DivergenceDetector.DivergenceDetectionResult divergences =
                DivergenceDetector.detectAll(data);

        // 4. 打印识别结果
        System.out.println("=== 背离识别结果 ===");
        System.out.println("顶背离数量: " + divergences.topDivergences.size());
        for (DivergenceResult d : divergences.topDivergences) {
            System.out.println("  " + d);
        }
        System.out.println("底背离数量: " + divergences.bottomDivergences.size());
        for (DivergenceResult d : divergences.bottomDivergences) {
            System.out.println("  " + d);
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MacdDivergenceDemo demo = new MacdDivergenceDemo();
            demo.pack();
            demo.setLocationRelativeTo(null);
            demo.setVisible(true);
        });
    }

    /**
     * 生成模拟数据
     */
    private List<StockDailyInfoVO> generateMockData(int count) {
        List<StockDailyInfoVO> data = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        Random rand = new Random(42);
        double price = 100.0;

        for (int i = 0; i < count; i++) {
            LocalDate date = startDate.plusDays(i);
            // 跳过周末
            if (date.getDayOfWeek().getValue() > 5) {
                continue;
            }

            double open = price;
            double change = (rand.nextDouble() - 0.5) * 6;
            double close = price + change;
            double high = Math.max(open, close) + rand.nextDouble() * 2;
            double low = Math.min(open, close) - rand.nextDouble() * 2;
            long volume = rand.nextLong(1000000, 5000000);

            StockDailyInfoVO vo = new StockDailyInfoVO();
            vo.setDate(date);
            vo.setOpenPrice(new BigDecimal(open));
            vo.setHighPrice(new BigDecimal(high));
            vo.setLowPrice(new BigDecimal(low));
            vo.setTodayClosePrice(new BigDecimal(close));
            vo.setTurnover(new BigDecimal(volume));
            data.add(vo);

            price = close;
            // 制造一些趋势变化，便于识别背离
            if (i > 50 && i < 80) price += 0.5;
            if (i > 120 && i < 150) price -= 0.8;
        }
        return data;
    }

}