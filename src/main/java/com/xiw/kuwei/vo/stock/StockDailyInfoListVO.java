package com.xiw.kuwei.vo.stock;

import com.diboot.core.binding.annotation.BindField;
import com.diboot.iam.entity.IamUser;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import com.xiw.kuwei.entity.stock.StockInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
* 股票每日信息 VO定义
* @author xiw
* @version 1.0
* @date 2026-04-07
* Copyright © xiwwwwww
*/
@Getter @Setter @Accessors(chain = true)
public class StockDailyInfoListVO extends StockDailyInfo  {
private static final long serialVersionUID = 1628349123558093167L;

    /**
    * 股票id 关联信息
    */
    @BindField(entity = StockInfo.class, field = "name", condition = "this.stock_id = id")
    private String stockIdLabel;

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