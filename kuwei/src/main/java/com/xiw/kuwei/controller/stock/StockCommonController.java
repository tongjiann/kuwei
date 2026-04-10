package com.xiw.kuwei.controller.stock;


import com.diboot.core.controller.BaseController;
import com.diboot.core.vo.JsonResult;
import com.xiw.kuwei.service.stock.StockCommonService;
import com.xiw.kuwei.service.stock.StockInfoService;
import com.xiw.kuwei.vo.stock.StockInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 股票每日信息
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

    @Resource
    private StockInfoService stockInfoService;

    @RequestMapping("init-stock-info")
    public JsonResult<?> initStockInfo(@RequestParam String code, @RequestParam String name) {

        stockCommonService.initStockInfo(code, name);
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
     * macd背离信号
     */
    @RequestMapping("macd-divergence")
    public JsonResult<?> macdDivergence(@RequestParam String stockId) {
        stockCommonService.macdDivergence(stockId);
        return JsonResult.OK();
    }


    /**
     * macd双金叉-双死叉信号
     */
    @RequestMapping("macd-signal")
    public JsonResult<?> macdSignal(@RequestParam String stockId) {
        stockCommonService.macdSignal(stockId);
        return JsonResult.OK();
    }

    /**
     * macd双金叉-双死叉信号
     */
    @RequestMapping("macd-signal-by-code")
    public JsonResult<?> macdSignalByCode(@RequestParam String code) {
        stockCommonService.macdSignalByCode(code);
        return JsonResult.OK();
    }

    /**
     * 多策略对比
     */
    @RequestMapping("multi-test")
    public JsonResult<?> multiTest(@RequestParam String code) {
        stockCommonService.multiTest(code);
        return JsonResult.OK();
    }


}
