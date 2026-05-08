package com.xiw.kuwei.vo.backtest;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TradeDetail {

    private String code;

    private String action; // 买 / 卖

    private BigDecimal price;

    private BigDecimal quantity;

    private BigDecimal amount;

    private BigDecimal pnl;

    private String description;

    private LocalDate date;

}