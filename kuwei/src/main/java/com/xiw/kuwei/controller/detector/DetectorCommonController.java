package com.xiw.kuwei.controller.detector;

import com.xiw.kuwei.service.detector.DetectorCommonService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/detector/common")
@RestController
public class DetectorCommonController {

    @Resource
    private DetectorCommonService detectorCommonService;

}
