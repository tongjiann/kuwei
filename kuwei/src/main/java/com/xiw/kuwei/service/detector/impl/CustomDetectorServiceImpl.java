package com.xiw.kuwei.service.detector.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.diboot.core.service.impl.BaseServiceImpl;
import com.diboot.iam.util.IamSecurityUtils;
import com.xiw.kuwei.constant.CommonEnum;
import com.xiw.kuwei.detector.DetectorFactory;
import com.xiw.kuwei.detector.DetectorInterface;
import com.xiw.kuwei.entity.detector.CustomDetector;
import com.xiw.kuwei.entity.detector.DetectorTemplateParameter;
import com.xiw.kuwei.exception.LogicalException;
import com.xiw.kuwei.mapper.detector.CustomDetectorMapper;
import com.xiw.kuwei.service.detector.CustomDetectorService;
import com.xiw.kuwei.service.detector.DetectorTemplateService;
import com.xiw.kuwei.vo.detector.DetectorTemplateVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * 自定义识别器 相关Service实现类
 *
 * @author xiw
 * @version 1.0
 * @date 2026-04-15
 * Copyright © xiwwwwww
 */
@Slf4j
@Service
public class CustomDetectorServiceImpl extends BaseServiceImpl<CustomDetectorMapper, CustomDetector> implements CustomDetectorService {

    @Resource
    private DetectorTemplateService detectorTemplateService;

    @Override
    public List<DetectorInterface> getCustomDetector() {
        return getCustomDetector(IamSecurityUtils.getCurrentUserId());
    }

    @Override
    public List<DetectorInterface> getCustomDetector(String userId) {
        List<CustomDetector> list = lambdaQuery()
                .eq(CustomDetector::getRelatedAccountId, userId)
                .eq(CustomDetector::getIsEnable, CommonEnum.TRUE.getCode())
                .list();
        List<DetectorInterface> result = new ArrayList<>();
        for (CustomDetector customDetector : list) {
            String templateId = customDetector.getTemplateId();
            String configParam = customDetector.getConfigParam();
            if (configParam == null) {
                throw new LogicalException("配置" + customDetector.getDisplayName() + "参数信息");
            }
            String key = templateId + ":" + configParam;
            DetectorInterface detector = DetectorFactory.getDetector(key);
            if (detector == null) {
                DetectorTemplateVO detectorTemplateVO = detectorTemplateService.getViewObject(templateId, DetectorTemplateVO.class);
                Class<DetectorInterface> detectorInterfaceClass = DetectorFactory.getDetectorClass(detectorTemplateVO.getName());
                JSON parsedJson = JSONUtil.parse(configParam);
                List<DetectorTemplateParameter> detectorTemplateParameterList = detectorTemplateVO.getDetectorTemplateParameterList();
                List<DetectorTemplateParameter> sorted = detectorTemplateParameterList.stream()
                        .sorted(Comparator.comparing(DetectorTemplateParameter::getIndex)).toList();
                Class[] sortedClassList = sorted.stream().map(DetectorTemplateParameter::getType).map(e -> switch (e) {
                            case "1" -> Integer.class;
                            case "2" -> BigDecimal.class;
                            default -> Object.class;
                        })
                        .toArray(Class<?>[]::new);
                try {
                    Constructor<DetectorInterface> declaredConstructor = detectorInterfaceClass.getDeclaredConstructor(sortedClassList);

                    List<Object> args = new ArrayList<>();
                    for (DetectorTemplateParameter detectorTemplateParameter : sorted) {
                        String type = detectorTemplateParameter.getType();
                        String code = detectorTemplateParameter.getCode();
                        Object byPath = parsedJson.getByPath(code);
                        switch (type) {
                            case "1":
                                if (byPath instanceof Integer i) {
                                    args.add(i);
                                }
                                break;
                            case "2":
                                if (byPath instanceof BigDecimal d) {
                                    args.add(d);
                                } else if (NumberUtil.isNumber(byPath.toString())) {
                                    args.add(new BigDecimal(byPath.toString()));
                                }
                        }
                    }
                    detector = declaredConstructor.newInstance(args.toArray());
                    DetectorFactory.addDetector(key, detector);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    log.error("查找构造器异常", e);
                    throw new LogicalException("配置异常，重新配置自定义识别器参数");
                }
            }
            result.add(detector);

        }
        return result;
    }

}