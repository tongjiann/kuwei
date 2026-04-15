package com.xiw.kuwei.entity.detector;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.diboot.core.entity.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 自定义识别器 Entity 定义
 * @author xiw
 * @version 1.0
 * @date 2026-04-15
 * Copyright © xiwwwwww
 */
@TableName("mdl_custom_detector")
@Getter
@Setter
@Accessors(chain = true)
public class CustomDetector extends BaseModel {

    private static final long serialVersionUID = 1632394869535164315L;

    /**
     * 模板
     */
    @TableField()
    private String templateId;

    /**
     * 配置参数
     */
    @Length(max = 100, message = "配置参数长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String configParam;

    /**
     * 创建者
     */
    @TableField()
    private String relatedAccountId;

    /**
     * 是否启用
     */
    @TableField()
    private String isEnable;

    /**
     * 展示名称
     */
    @Length(max = 100, message = "展示名称长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String displayName;
}
