package com.xiw.kuwei.dto.ding;

import cn.hutool.core.text.CharSequenceUtil;
import com.xiw.kuwei.vo.backtest.Signal;
import com.xiw.kuwei.vo.stock.StockInfoVO;
import com.xiw.kuwei.vo.stock.StockSignalVO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class SignalMarkdown extends StockSignalVO {


    private static final String TITLE_TEMPLATE = "## {}-{}\n";

    private static final String MAIN_CONTENT_TEMPLATE = "- 【{}】:{}\n";

    @Override
    public String toString() {
        LocalDate now = LocalDate.now();
        StockInfoVO stockInfoVO = getStockInfoVO();
        String name = stockInfoVO.getName();
        String code = stockInfoVO.getCode();
        String title = CharSequenceUtil.format(TITLE_TEMPLATE, code, name);
        Map<LocalDate, List<Signal>> dateSignalMap = getDateSignalMap();
        String mainContent;
        if (dateSignalMap.containsKey(now)) {
            List<Signal> signals = dateSignalMap.get(now);
            mainContent = signals.stream()
                    .map(e -> CharSequenceUtil.format(MAIN_CONTENT_TEMPLATE, e.getSign() == 0 ? "买" : "卖", e.getDescription()))
                    .collect(Collectors.joining());
        } else {
            mainContent = "无信号\n";
        }

        return title + mainContent + "---\n";

    }

}
