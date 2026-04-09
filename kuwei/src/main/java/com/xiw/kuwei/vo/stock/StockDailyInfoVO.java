package com.xiw.kuwei.vo.stock;

import com.xiw.kuwei.calculator.MaInfo;
import com.xiw.kuwei.calculator.MacdInfo;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockDailyInfoVO extends StockDailyInfo {

    private MacdInfo macdInfo;

    private MaInfo maInfo;

}
