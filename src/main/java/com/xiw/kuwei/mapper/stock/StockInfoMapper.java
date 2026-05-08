package com.xiw.kuwei.mapper.stock;

import com.diboot.core.mapper.BaseCrudMapper;
import com.xiw.kuwei.entity.stock.StockInfo;
import org.apache.ibatis.annotations.Mapper;

/**
* 股票基础信息 相关Mapper层接口定义
* @author xiw
* @version 1.0
* @date 2026-04-07
* Copyright © xiwwwwww
*/
@Mapper
public interface StockInfoMapper extends BaseCrudMapper<StockInfo> {

}