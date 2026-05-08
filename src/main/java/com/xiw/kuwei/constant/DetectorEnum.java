package com.xiw.kuwei.constant;

import com.xiw.kuwei.detector.DetectorInterface;
import lombok.Getter;

@Getter
@Deprecated
public enum DetectorEnum {
    ;


    private final String name;

    private final DetectorInterface detectorInterface;

    DetectorEnum(DetectorInterface detectorInterface) {
        this.name = detectorInterface.getDetectorName();
        this.detectorInterface = detectorInterface;
    }
}
