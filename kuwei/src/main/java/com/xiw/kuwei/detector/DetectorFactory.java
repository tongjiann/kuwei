package com.xiw.kuwei.detector;

import com.xiw.kuwei.constant.DetectorEnum;

import java.util.HashMap;
import java.util.Map;

public class DetectorFactory {

    private static final Map<Integer, Class<? extends DetectorInterface>> classMap = new HashMap<>();

    static {
        for (DetectorEnum value : DetectorEnum.values()) {
            classMap.put(value.getType(), value.getClazz());
        }
    }

    public static DetectorInterface getDetector(int type) {
        Class<? extends DetectorInterface> clazz = classMap.get(type);
        if (clazz == null) throw new NullPointerException();
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create detector", e);
        }
    }

}
