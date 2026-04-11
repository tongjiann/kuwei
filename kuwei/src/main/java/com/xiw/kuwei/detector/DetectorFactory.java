package com.xiw.kuwei.detector;

import com.xiw.kuwei.constant.DetectorEnum;

import java.util.HashMap;
import java.util.Map;

public class DetectorFactory {

    private static final Map<String, DetectorInterface> INSTANCE_MAP = new HashMap<>();

    static {
        for (DetectorEnum value : DetectorEnum.values()) {
            INSTANCE_MAP.put(value.getName(), value.getDetectorInterface());
        }
    }

    public static DetectorInterface getDetector(String name) {
        DetectorInterface instance = INSTANCE_MAP.get(name);
        if (instance == null) {
            throw new NullPointerException();
        }
        return instance;
    }

}
