package com.xiw.kuwei.vo.detector;

import com.diboot.core.binding.annotation.BindDict;
import com.diboot.core.binding.annotation.BindField;
import com.diboot.core.vo.LabelValue;
import com.diboot.iam.entity.IamUser;
import com.xiw.kuwei.entity.detector.DetectorTemplate;
import com.xiw.kuwei.entity.detector.DetectorTemplateParameter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 识别器模板参数 VO定义
 * @author xiw
 * @version 1.0
 * @date 2026-04-15
 * Copyright © xiwwwwww
 */
@Getter
@Setter
@Accessors(chain = true)
public class DetectorTemplateParameterListVO extends DetectorTemplateParameter {

    private static final long serialVersionUID = 1658549998042824089L;

    /**
     * 模板 关联信息
     */
    @BindField(entity = DetectorTemplate.class, field = "name", condition = "this.template_id = id")
    private String templateIdLabel;

    /**
     * 创建人 关联信息
     */
    @BindField(entity = IamUser.class, field = "realname", condition = "this.create_by = id")
    private String createByLabel;

    /**
     * 更新人 关联信息
     */
    @BindField(entity = IamUser.class, field = "realname", condition = "this.update_by = id")
    private String updateByLabel;

    /**
     * 引用字段：模板名称
     */
    @BindField(entity = DetectorTemplate.class, field = "name", condition = "this.template_id = id")
    private String templateName;

    /**
     * 类型 关联字典选项
     */
    @BindDict(type = "PARAM_TYPE", field = "type")
    private LabelValue typeLabel;
}
