package com.xiw.kuwei.detector;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import com.xiw.kuwei.annotation.Detector;
import com.xiw.kuwei.constant.DetectorEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DetectorFactory {

    private static final Map<String, DetectorInterface> INSTANCE_MAP = new HashMap<>();

    private static final Map<String, Class<DetectorInterface>> DETECTOR_TEMPLATE_MAP = new HashMap<>();

    static {
        for (DetectorEnum value : DetectorEnum.values()) {
            INSTANCE_MAP.put(value.getName(), value.getDetectorInterface());
        }
    }

    public static DetectorInterface getDetector(String name) {
        return INSTANCE_MAP.get(name);
    }

    public static void addDetector(String name, DetectorInterface detectorInterface) {
        INSTANCE_MAP.put(name, detectorInterface);
    }

    public static Class<DetectorInterface> getDetectorClass(String name) {
        if (CollUtil.isEmpty(DETECTOR_TEMPLATE_MAP)) {
            lazyInitDetectorTemplateMap();
        }
        return DETECTOR_TEMPLATE_MAP.get(name);
    }

    private static void lazyInitDetectorTemplateMap() {
        Set<Class<?>> classes = ClassUtil.scanPackage("com.xiw.kuwei.detector");
        for (Class<?> o : classes) {
            if (DetectorInterface.class.isAssignableFrom(o) && o.isAnnotationPresent(Detector.class)) {
                Detector annotation = o.getAnnotation(Detector.class);
                DETECTOR_TEMPLATE_MAP.put(annotation.name(), (Class<DetectorInterface>) o);
            }
        }
    }


}
