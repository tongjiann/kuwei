package com.xiw.kuwei.detector;

import com.xiw.kuwei.vo.backtest.Signal;
import com.xiw.kuwei.vo.stock.StockDailyInfoVO;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public interface DetectorInterface {

    private static @NotNull List<Signal> getUniqueSortedList(List<Signal> signalList) {
        // 去重 + 排序（和你原来一致）
        Set<String> seenKeys = new LinkedHashSet<>();
        List<Signal> result = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        for (Signal s : signalList) {
            String key = s.getDateTime().toLocalDate().format(formatter)
                    + "|" + s.getSign() + "|" + s.getDescription();

            if (seenKeys.add(key)) {
                result.add(s);
            }
        }

        result.sort(Comparator.comparing(Signal::getDateTime));
        return result;
    }

    String getDetectorName();

    default Signal buildSignal(int sign, String code, StockDailyInfoVO vo, BigDecimal strength, String desc) {
        Signal signal = new Signal();
        signal.setSign(sign);
        signal.setCode(code);
        signal.setDescription(desc);
        signal.setPrice(vo.getTodayClosePrice());

        LocalDate date = vo.getDate();
        signal.setDateTime(LocalDateTime.of(date, LocalTime.of(15, 0)));
        signal.setStrength(strength);
        return signal;
    }

    default List<Signal> detectSignals(List<StockDailyInfoVO> data, String code) {
        List<Signal> signalList = doDetectSignals(data, code);

        return getUniqueSortedList(signalList);
    }

    List<Signal> doDetectSignals(List<StockDailyInfoVO> data, String code);

}
