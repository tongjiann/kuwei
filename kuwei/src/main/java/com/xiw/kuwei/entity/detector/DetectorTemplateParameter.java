package com.xiw.kuwei.entity.detector;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.diboot.core.entity.BaseModel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * 识别器模板参数 Entity 定义
 * @author xiw
 * @version 1.0
 * @date 2026-04-15
 * Copyright © xiwwwwww
 */
@TableName("mdl_detector_template_parameter")
@Getter
@Setter
@Accessors(chain = true)
public class DetectorTemplateParameter extends BaseModel {

    private static final long serialVersionUID = 1425120133617591458L;

    /**
     * 模板
     */
    @NotNull(message = "模板不能为空")
    @TableField()
    private String templateId;

    /**
     * 名称
     */
    @NotNull(message = "名称不能为空")
    @Length(max = 100, message = "名称长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String parameterName;

    /**
     * 编码
     */
    @NotNull(message = "编码不能为空")
    @Length(max = 100, message = "编码长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String code;

    /**
     * 类型
     */
    @NotNull(message = "类型不能为空")
    @TableField()
    private String type;

    /**
     * 默认值
     */
    @TableField()
    private BigDecimal defaultValue;

    /**
     * 说明
     */
    @Length(max = 100, message = "说明长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String description;

    /**
     * 序号
     */
    @NotNull(message = "序号不能为空")
    @TableField(value = "`index`")
    private Long index;
}
