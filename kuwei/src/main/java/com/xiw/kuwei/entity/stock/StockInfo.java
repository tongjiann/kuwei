package com.xiw.kuwei.entity.stock;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.diboot.core.entity.BaseModel;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

/**
 * 股票基础信息 Entity 定义
 *
 * @author xiw
 * @version 1.0
 * @date 2026-04-07
 * Copyright © xiwwwwww
 */
@TableName("mdl_stock_info")
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StockInfo extends BaseModel {

    private static final long serialVersionUID = 1253040600809958653L;

    /**
     * 名称
     */
    @Length(max = 100, message = "名称长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String name;

    /**
     * 编码
     */
    @NotNull(message = "编码不能为空")
    @Length(max = 20, message = "编码长度应小于20")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String code;

    /**
     * 额外
     */
    @TableField()
    private String extraInfo;

    /**
     * 板块
     */
    @Length(max = 100, message = "板块长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String section;

    /**
     * 信息
     */
    @Length(max = 100, message = "信息长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField()
    private String info;

    /**
     * 交易所
     */
    @Length(max = 100, message = "交易所长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField(value = "`exchange`")
    private String exchange;

    /**
     * 来源
     */
    @Length(max = 100, message = "来源长度应小于100")
    @BindQuery(comparison = Comparison.LIKE)
    @TableField(value = "`source`")
    private String source;

    /**
     * 最新数据更新时间
     */
    @TableField()
    private LocalDate latestDataFreshTime;
}
