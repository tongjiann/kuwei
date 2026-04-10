package com.xiw.kuwei.detector;

import com.xiw.kuwei.vo.stock.StockDailyInfoVO;

import java.util.List;

public interface DetectorInterface {

    List<Signal> detectSignals(List<StockDailyInfoVO> data, String code);


}
