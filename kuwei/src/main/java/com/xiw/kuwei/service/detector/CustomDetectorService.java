package com.xiw.kuwei.service.detector;

import com.diboot.core.service.BaseService;
import com.xiw.kuwei.detector.DetectorInterface;
import com.xiw.kuwei.entity.detector.CustomDetector;

import java.util.List;

/**
* 自定义识别器 相关Service接口定义
* @author xiw
* @version 1.0
* @date 2026-04-15
* Copyright © xiwwwwww
*/
public interface CustomDetectorService extends BaseService<CustomDetector> {

    List<DetectorInterface> getCustomDetector();

}