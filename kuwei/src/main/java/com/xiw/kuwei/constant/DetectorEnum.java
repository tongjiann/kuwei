package com.xiw.kuwei.constant;

import com.xiw.kuwei.detector.DetectorInterface;
import com.xiw.kuwei.detector.DoubleMASignalDetector;
import lombok.Getter;

@Getter
public enum DetectorEnum {
    MA10_30(new DoubleMASignalDetector(10, 30)),
    MA5_15(new DoubleMASignalDetector(5, 15)),
    MA20_60(new DoubleMASignalDetector(20, 60)),
    ;


    private final String name;

    private final DetectorInterface detectorInterface;

    DetectorEnum(DetectorInterface detectorInterface) {
        this.name = detectorInterface.getDetectorName();
        this.detectorInterface = detectorInterface;
    }
}
