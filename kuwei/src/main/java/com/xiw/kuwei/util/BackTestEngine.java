package com.xiw.kuwei.util;

import com.xiw.kuwei.detector.BackTestRecord;
import com.xiw.kuwei.detector.Signal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MACD 信号回测引擎
 */
public class BackTestEngine {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    /**
     * 运行回测
     *
     * @param signals     按时间升序排列的信号列表
     * @param initialCash 初始现金
     * @param code        股票代码（用于校验，可为空）
     * @return 回测记录列表（包含初始状态和每次交易后状态）
     */
    public static List<BackTestRecord> runBackTest(List<Signal> signals,
                                                   BigDecimal initialCash,
                                                   String code) {
        List<BackTestRecord> records = new ArrayList<>();

        // 初始化状态
        BigDecimal cash = initialCash;
        BigDecimal position = BigDecimal.ZERO;           // 持仓股数
        BigDecimal positionValue = BigDecimal.ZERO;      // 持仓市值
        BigDecimal totalAsset = initialCash;             // 总资产

        // 添加初始记录
        records.add(createRecord(null, code, "起始", null, cash, position, totalAsset));

        for (Signal signal : signals) {
            // 可选的股票代码过滤
            if (code != null && !code.isEmpty() && !code.equals(signal.getCode())) {
                continue;
            }

            BigDecimal strength = signal.getStrength();
            if (strength == null || strength.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // 强度为0，忽略信号
            }

            BigDecimal price = signal.getPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // 价格无效
            }

            int sign = signal.getSign(); // 0买 1卖
            LocalDateTime dateTime = signal.getDateTime();
            String action;

            if (sign == 0) { // 买入
                if (cash.compareTo(BigDecimal.ZERO) <= 0) continue;

                // 买入金额 = min(现金余额, 总资产 * 强度)
                BigDecimal maxBuyAmount = totalAsset.multiply(strength);
                BigDecimal buyAmount = cash.min(maxBuyAmount);

                if (buyAmount.compareTo(BigDecimal.ZERO) <= 0) continue;

                // 买入股数 = 买入金额 / 价格（保留2位小数，模拟实际可买非整数股）
                BigDecimal shares = buyAmount.divide(price, 2, RoundingMode.HALF_DOWN);

                // 实际花费金额 = 股数 * 价格（可能存在微小误差）
                BigDecimal actualCost = shares.multiply(price);

                cash = cash.subtract(actualCost);
                position = position.add(shares);
                action = "买";

            } else { // 卖出
                if (position.compareTo(BigDecimal.ZERO) <= 0) continue;

                // 持仓市值
                positionValue = position.multiply(price);
                totalAsset = cash.add(positionValue);

                // 卖出金额 = 持仓市值 * 强度
                BigDecimal sellAmount = positionValue.multiply(strength);
                if (sellAmount.compareTo(BigDecimal.ZERO) <= 0) continue;

                // 卖出股数 = 卖出金额 / 价格
                BigDecimal sellShares = sellAmount.divide(price, 2, RoundingMode.HALF_DOWN);
                // 不能超过现有持仓
                sellShares = sellShares.min(position);

                // 实际卖出收入
                BigDecimal actualRevenue = sellShares.multiply(price);

                cash = cash.add(actualRevenue);
                position = position.subtract(sellShares);
                action = "卖";
            }

            // 更新持仓市值与总资产
            positionValue = position.multiply(price);
            totalAsset = cash.add(positionValue);

            // 创建本次交易记录
            BackTestRecord record = createRecord(dateTime, signal.getCode(), action,
                    price, cash, position, totalAsset);
            // 添加本次交易金额
            if (sign == 0) {
                record.setTradeAmount(price.multiply(position).subtract(
                        records.isEmpty() ? BigDecimal.ZERO : records.get(records.size() - 1).getPositionValue()
                ).abs()); // 简化，使用买入花费
            } else {
                record.setTradeAmount(price.multiply(position).abs()); // 实际收入
            }
            records.add(record);
        }

        return records;
    }

    private static BackTestRecord createRecord(LocalDateTime dateTime,
                                               String code,
                                               String action,
                                               BigDecimal price,
                                               BigDecimal cash,
                                               BigDecimal position,
                                               BigDecimal totalAsset) {
        BackTestRecord record = new BackTestRecord();
        record.setDateTime(dateTime);
        record.setCode(code);
        record.setAction(action);
        record.setPrice(price);
        record.setCash(cash);
        record.setPosition(position);
        record.setPositionValue(position.multiply(price != null ? price : BigDecimal.ZERO));
        record.setTotalAsset(totalAsset);
        record.setTradeAmount(BigDecimal.ZERO); // 外部设置
        return record;
    }


    /**
     * 批量运行多个策略的回测
     *
     * @param strategySignals 策略名称 → 信号列表
     * @param initialCash     初始资金（所有策略共用同一初始资金，独立运行）
     * @param code            股票代码（用于过滤，可留空）
     * @return 策略名称 → 回测记录列表
     */
    public static Map<String, List<BackTestRecord>> runMultiStrategyBackTest(
            Map<String, List<Signal>> strategySignals,
            BigDecimal initialCash,
            String code) {

        Map<String, List<BackTestRecord>> results = new LinkedHashMap<>();

        for (Map.Entry<String, List<Signal>> entry : strategySignals.entrySet()) {
            String strategyName = entry.getKey();
            List<Signal> signals = entry.getValue();
            List<BackTestRecord> records = runBackTest(signals, initialCash, code);
            results.put(strategyName, records);
        }

        return results;
    }

}