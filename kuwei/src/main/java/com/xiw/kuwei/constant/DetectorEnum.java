package com.xiw.kuwei.constant;

import com.xiw.kuwei.annotation.Detector;
import com.xiw.kuwei.detector.DetectorInterface;
import com.xiw.kuwei.detector.Ma10SignalDetector;
import com.xiw.kuwei.detector.Ma20SignalDetector;
import com.xiw.kuwei.detector.MacdSignalDetector;
import lombok.Getter;

@Getter
public enum DetectorEnum {
    MACD(1, MacdSignalDetector.class),
    MA20(2, Ma20SignalDetector.class),
    MA10(3, Ma10SignalDetector.class),
    ;

    private final int type;

    private final String name;

    private final Class<? extends DetectorInterface> clazz;

    DetectorEnum(int type, Class<? extends DetectorInterface> clazz) {
        this.type = type;
        Detector annotation = clazz.getAnnotation(Detector.class);
        this.name = annotation.name();
        this.clazz = clazz;
    }
}
