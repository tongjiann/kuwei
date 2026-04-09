package com.xiw.kuwei.detector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Signal {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 0买1卖
     */
    private int sign;

    /**
     * 编码
     */
    private String code;

    /**
     * 信号强度，范围0-1
     */
    private BigDecimal strength;

    /**
     * 操作日期时间
     */
    private LocalDateTime dateTime;

    /**
     * 操作价格
     */
    private BigDecimal price;

    /**
     * 操作说明
     */
    private String description;

    /**
     * 自定义格式化输出，替代 Lombok 生成的 toString
     */
    @Override
    public String toString() {
        String direction = sign == 0 ? "买" : "卖";
        String formattedDateTime = dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "null";
        String formattedPrice = price != null ? price.setScale(2, RoundingMode.HALF_UP).toString() : "null";
        String strengthPercent = strength != null
                ? strength.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%"
                : "null";

        return String.format("[%s] %s %s | 价格=%s | 强度=%s | 说明=%s",
                formattedDateTime, code, direction, formattedPrice, strengthPercent, description);
    }

}