package com.xiw.kuwei.vo.detector;

import com.diboot.core.binding.annotation.BindDict;
import com.diboot.core.binding.annotation.BindField;
import com.diboot.core.vo.LabelValue;
import com.diboot.iam.entity.IamUser;
import com.xiw.kuwei.entity.detector.CustomDetector;
import com.xiw.kuwei.entity.detector.DetectorTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 自定义识别器 VO定义
 * @author xiw
 * @version 1.0
 * @date 2026-04-15
 * Copyright © xiwwwwww
 */
@Getter
@Setter
@Accessors(chain = true)
public class CustomDetectorListVO extends CustomDetector {

    private static final long serialVersionUID = 1291957241137818705L;

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
     * 创建者 关联信息
     */
    @BindField(entity = IamUser.class, field = "realname", condition = "this.related_account_id = id")
    private String relatedAccountIdLabel;

    /**
     * 是否启用 关联字典选项
     */
    @BindDict(type = "TRUE_FALSE_DIC", field = "isEnable")
    private LabelValue isEnableLabel;
}
