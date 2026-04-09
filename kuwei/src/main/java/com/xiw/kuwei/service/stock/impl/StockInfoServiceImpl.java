package com.xiw.kuwei.service.stock.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.diboot.core.util.S;
import com.diboot.core.vo.Status;
import com.diboot.core.service.impl.BaseServiceImpl;
import com.diboot.core.exception.BusinessException;

import com.xiw.kuwei.entity.stock.StockInfo;

import com.xiw.kuwei.mapper.stock.StockInfoMapper;
import com.xiw.kuwei.service.stock.StockInfoService;


/**
* 股票基础信息 相关Service实现类
* @author xiw
* @version 1.0
* @date 2026-04-07
* Copyright © xiwwwwww
*/
@Slf4j
@Service
public class StockInfoServiceImpl extends BaseServiceImpl<StockInfoMapper, StockInfo> implements StockInfoService {

}