package com.xiw.kuwei.vo.stock;

import com.xiw.kuwei.entity.stock.StockInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class StockInfoVO extends StockInfo {

    private List<StockDailyInfoVO> stockDailyInfoVOList;

}
