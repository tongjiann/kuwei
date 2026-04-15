package com.xiw.kuwei.controller.stock;


import cn.hutool.core.collection.CollUtil;
import com.diboot.core.controller.BaseController;
import com.diboot.core.vo.JsonResult;
import com.xiw.kuwei.service.stock.StockCommonService;
import com.xiw.kuwei.vo.backtest.PortfolioBackTestResult;
import com.xiw.kuwei.vo.stock.StockInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 股票
 *
 * @author MyName
 * @version 1.0
 * @date 2026-04-07
 * Copyright © MyCorp
 */
@Slf4j
@RequestMapping("/stock/common")
@RestController
public class StockCommonController extends BaseController {

    @Resource
    private StockCommonService stockCommonService;

    @RequestMapping("init-stock-info")
    public JsonResult<?> initStockInfo(@RequestParam String code, @RequestParam String name) {

        stockCommonService.initStockInfo(code, name);
        return JsonResult.OK();
    }

    @RequestMapping("update-stock-info")
    public JsonResult<?> updateStockInfo() {

        stockCommonService.updateStockInfo();
        return JsonResult.OK();
    }

    @RequestMapping("update-stock-daily-info")
    public JsonResult<?> updateStockDailyInfo() {

        stockCommonService.updateStockDailyInfo();
        return JsonResult.OK();
    }

    @RequestMapping("get-extra-info")
    public JsonResult<?> getExtraInfo(@RequestParam String stockId) {
        int days = 120;
        StockInfoVO stockInfoVO = stockCommonService.getExtraStockInfo(stockId, days);

        return JsonResult.OK(stockInfoVO);
    }


    @RequestMapping("create-charts")
    public JsonResult<?> createCharts() {
        int days = 120;

        stockCommonService.createCharts(days);
        return JsonResult.OK();
    }

    /**
     * 对单一股票进行多策略对比
     */
    @RequestMapping("multi-test")
    public JsonResult<?> multiTest(@RequestParam(required = false) String code, @RequestParam(required = false) String startDateStr) {

        LocalDate startDate = null;
        if (startDateStr != null) {
            try {
                startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (Exception ignored) {
            }
        }
        List<PortfolioBackTestResult> data = stockCommonService.multiTest(code, startDate);
        if (code != null) {

            return JsonResult.OK(data);
        }
        return JsonResult.OK();
    }

    /**
     * 对多股票进行多策略对比
     */
    @RequestMapping("multi-test-by-code-list")
    public JsonResult<?> multiTestByCodeList(@RequestBody Map<String, Object> map) {
        Object codeListObj = map.get("codeList");
        Object startDateObj = map.get("startDate");
        // if (!(codeListObj instanceof List list) || CollUtil.isEmpty((list))) {
        //     return JsonResult.FAIL_INVALID_PARAM("codeList");
        // }
        LocalDate startDate = null;
        if (startDateObj instanceof String startDateStr) {
            try {
                startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (Exception ignored) {
            }
        }
        List<PortfolioBackTestResult> data = stockCommonService.multiTestByCodeList((List<String>) codeListObj, startDate);
        if (CollUtil.isEmpty((Collection<?>) codeListObj)) {
            return JsonResult.OK();
        }
        return JsonResult.OK(data);
    }

    @RequestMapping("sync-daily-info")
    public JsonResult<?> syncDailyInfo(@RequestParam(required = false) String stockId) {
        stockCommonService.syncDailyInfo(stockId);
        return JsonResult.OK();
    }


}
