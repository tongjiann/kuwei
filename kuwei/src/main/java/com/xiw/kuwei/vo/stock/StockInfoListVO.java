package com.xiw.kuwei.vo.stock;

import com.diboot.core.binding.annotation.*;
import com.diboot.core.vo.FileVO;
import com.diboot.core.vo.LabelValue;
import com.diboot.iam.entity.IamOrg;
import com.diboot.iam.entity.IamPosition;
import com.diboot.iam.entity.IamRole;
import com.diboot.iam.entity.IamUser;
import com.diboot.file.entity.FileRecord;
import com.xiw.kuwei.entity.stock.StockInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.time.LocalDate;

/**
* 股票基础信息 VO定义
* @author xiw
* @version 1.0
* @date 2026-04-07
* Copyright © xiwwwwww
*/
@Getter @Setter @Accessors(chain = true)
public class StockInfoListVO extends StockInfo  {
private static final long serialVersionUID = 1786833938235132590L;

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