package com.xiw.kuwei.vo.detector;

import com.diboot.core.binding.annotation.BindField;
import com.diboot.iam.entity.IamUser;
import com.xiw.kuwei.entity.detector.DetectorTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
* 识别器模板 VO定义
* @author xiw
* @version 1.0
* @date 2026-04-15
* Copyright © xiwwwwww
*/
@Getter @Setter @Accessors(chain = true)
public class DetectorTemplateListVO extends DetectorTemplate  {
private static final long serialVersionUID = 1388844647325609790L;

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

}