package com.xiw.kuwei.vo.stock;

import com.xiw.kuwei.calculator.MacdInfo;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockDailyInfoVO extends StockDailyInfo {

    private MacdInfo macdInfo;

    private Map<Integer, BigDecimal> maInfo;

}
