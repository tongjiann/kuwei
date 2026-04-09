package com.xiw.kuwei.detector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackTestRecord {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime dateTime;

    private String code;

    private String action; // 买 / 卖

    private BigDecimal price;

    private BigDecimal tradeAmount; // 本次交易金额

    private BigDecimal cash; // 剩余现金

    private BigDecimal position; // 持仓股数

    private BigDecimal positionValue; // 持仓市值

    private BigDecimal totalAsset; // 总资产

    @Override
    public String toString() {
        String formattedDateTime = dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "null";
        String actionDisplay = action != null ? action : "未知";
        String formattedPrice = formatDecimal(price, 2);
        String formattedTradeAmount = formatDecimal(tradeAmount, 2);
        String formattedCash = formatDecimal(cash, 2);
        String formattedPosition = formatDecimal(position, 0); // 股数通常为整数
        String formattedPositionValue = formatDecimal(positionValue, 2);
        String formattedTotalAsset = formatDecimal(totalAsset, 2);

        return String.format(
                "[%s] %s %s | 价格=%s | 交易额=%s | 现金=%s | 持仓=%s股 | 市值=%s | 总资产=%s",
                formattedDateTime,
                code != null ? code : "null",
                actionDisplay,
                formattedPrice,
                formattedTradeAmount,
                formattedCash,
                formattedPosition,
                formattedPositionValue,
                formattedTotalAsset
        );
    }

    /**
     * 格式化 BigDecimal，保留指定小数位数，null 时返回 "null"
     */
    private String formatDecimal(BigDecimal value, int scale) {
        if (value == null) return "null";
        return value.setScale(scale, RoundingMode.HALF_UP).toString();
    }
}