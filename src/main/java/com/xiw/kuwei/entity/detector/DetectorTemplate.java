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

/**
* 识别器模板 Entity 定义
* @author xiw
* @version 1.0
* @date 2026-04-15
* Copyright © xiwwwwww
*/
@TableName("mdl_detector_template")
@Getter @Setter @Accessors(chain = true)
public class DetectorTemplate extends BaseModel {
    private static final long serialVersionUID = 1917274159481338581L;
     /**
     * 名称
     */
     @NotNull(message = "名称不能为空")
     @Length(max=100, message="名称长度应小于100")
     @BindQuery(comparison = Comparison.LIKE)
     @TableField()
     private String name;

     /**
     * 描述
     */
     @Length(max=100, message="描述长度应小于100")
     @BindQuery(comparison = Comparison.LIKE)
     @TableField()
     private String description;

     /**
     * 参数数量
     */
     @TableField()
     private Long parameterCount;


}