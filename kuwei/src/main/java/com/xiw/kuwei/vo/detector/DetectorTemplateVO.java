package com.xiw.kuwei.vo.detector;

import com.diboot.core.binding.annotation.BindEntityList;
import com.xiw.kuwei.entity.detector.DetectorTemplate;
import com.xiw.kuwei.entity.detector.DetectorTemplateParameter;
import lombok.Data;

import java.util.List;

@Data
public class DetectorTemplateVO extends DetectorTemplate {

    @BindEntityList(entity = DetectorTemplateParameter.class, condition = "this.id=template_id")
    List<DetectorTemplateParameter> detectorTemplateParameterList;

}
