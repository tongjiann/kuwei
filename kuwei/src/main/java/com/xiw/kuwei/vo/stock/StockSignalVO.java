package com.xiw.kuwei.vo.stock;

import com.xiw.kuwei.vo.backtest.Signal;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class StockSignalVO {

    private StockInfoVO stockInfoVO;

    private Map<LocalDate, List<Signal>> dateSignalMap;

}
