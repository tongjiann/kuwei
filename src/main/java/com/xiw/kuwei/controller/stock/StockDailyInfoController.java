package com.xiw.kuwei.controller.stock;

import com.diboot.core.controller.BaseCrudRestController;
import com.diboot.core.vo.JsonResult;
import com.diboot.core.vo.Pagination;
import com.diboot.iam.annotation.BindPermission;
import com.diboot.iam.annotation.Log;
import com.diboot.iam.annotation.OperationCons;
import com.xiw.kuwei.dto.stock.StockDailyInfoDTO;
import com.xiw.kuwei.entity.stock.StockDailyInfo;
import com.xiw.kuwei.service.stock.StockDailyInfoService;
import com.xiw.kuwei.vo.stock.StockDailyInfoDetailVO;
import com.xiw.kuwei.vo.stock.StockDailyInfoListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
* 股票每日信息 相关rest接口
* @author xiw
* @version 1.0
* @date 2026-04-07
* Copyright © xiwwwwww
*/
@Slf4j
@RequestMapping("/stock/stock-daily-info")
@BindPermission(name = "股票每日信息")
@RestController
public class StockDailyInfoController extends BaseCrudRestController<StockDailyInfo> {

    @Autowired
    private StockDailyInfoService stockDailyInfoService;

    /**
    * 查询资源对象的列表VO记录
    * <p>
    * url请求参数示例: ?fieldA=abc&pageSize=20&pageIndex=1&orderBy=id
    * </p>
    * @return
    * @throws Exception
    */
    @Log(operation = OperationCons.LABEL_LIST)
    @BindPermission(name = OperationCons.LABEL_LIST, code = OperationCons.CODE_READ)
    @GetMapping()
    public JsonResult<List<StockDailyInfoListVO>> getListVOMapping(StockDailyInfoDTO queryDto, Pagination pagination) throws Exception {
        return super.getViewObjectList(queryDto, pagination, StockDailyInfoListVO.class);
    }

    @RequestMapping("get-k-line-data-by-stock-id")
    public JsonResult<List<StockDailyInfo>> getKLineDataByStockId(@RequestParam String stockId) {

        return JsonResult.OK(stockDailyInfoService.lambdaQuery()
                .eq(StockDailyInfo::getStockId, stockId)
                .orderByAsc(StockDailyInfo::getDate)
                .list());
    }

    /**
    * 根据id查询资源对象的详情VO
    * @param id ID
    * @return
    * @throws Exception
    */
    @Log(operation = OperationCons.LABEL_DETAIL)
    @BindPermission(name = OperationCons.LABEL_DETAIL, code = OperationCons.CODE_READ)
    @GetMapping("/{id}")
    public JsonResult<StockDailyInfoDetailVO> getDetailVOMapping(@PathVariable("id")String id) throws Exception {
        StockDailyInfoDetailVO detailVO = stockDailyInfoService.getViewObject(id, StockDailyInfoDetailVO.class);
        return JsonResult.OK(detailVO);
    }

/**
    * 根据指定字段和值查询资源对象的详情VO
    * @param fieldName 字段名
    * @param fieldValue 字段值
    * @return
    * @throws Exception
    */
    @BindPermission(name = OperationCons.LABEL_DETAIL, code = OperationCons.CODE_READ)
    @GetMapping("/{fieldName}/{fieldValue}")
    public JsonResult<StockDailyInfoDetailVO> getDetailVOMapping(@PathVariable("fieldName") String fieldName, @PathVariable("fieldValue") String fieldValue) throws Exception {
        StockDailyInfoDetailVO detailVO = stockDailyInfoService.getViewObject(fieldName, fieldValue, StockDailyInfoDetailVO.class);
        return JsonResult.OK(detailVO);
    }


    /**
    * 创建资源对象数据
    * @param stockDailyInfo
    * @return JsonResult
    * @throws Exception
    */
    @Log(operation = OperationCons.LABEL_CREATE)
    @BindPermission(name = OperationCons.LABEL_CREATE, code = OperationCons.CODE_WRITE)
    @PostMapping()
    public JsonResult<Serializable> createModelMapping(@RequestBody StockDailyInfo stockDailyInfo) throws Exception {
        stockDailyInfoService.createEntity(stockDailyInfo);
        return JsonResult.OK(stockDailyInfo.getId());
    }

    /**
    * 根据id更新资源对象
    * @param stockDailyInfo
    * @return JsonResult
    * @throws Exception
    */
    @Log(operation = OperationCons.LABEL_UPDATE)
    @BindPermission(name = OperationCons.LABEL_UPDATE, code = OperationCons.CODE_WRITE)
    @PutMapping("/{id}")
    public JsonResult<?> updateModelMapping(@PathVariable("id")String id, @RequestBody StockDailyInfo stockDailyInfo) throws Exception {
        stockDailyInfoService.updateEntity(stockDailyInfo);
        return JsonResult.OK();
    }

    /**
    * 根据id删除资源对象
    * @param id
    * @return
    * @throws Exception
    */
    @Log(operation = OperationCons.LABEL_DELETE)
    @BindPermission(name = OperationCons.LABEL_DELETE, code = OperationCons.CODE_WRITE)
    @DeleteMapping("/{id}")
    public JsonResult<?> deleteModelMapping(@PathVariable("id") String id) throws Exception {
        boolean success = stockDailyInfoService.deleteEntity(id);
        log.debug("删除数据 股票每日信息:{} {}", id, success? "成功":"失败");
        return JsonResult.OK();
    }

    /**
    * 根据ids集合删除资源对象
    * @param ids
    * @return
    * @throws Exception
    */
    @Log(operation = OperationCons.LABEL_BATCH_DELETE)
    @BindPermission(name = OperationCons.LABEL_DELETE, code = OperationCons.CODE_WRITE)
    @PostMapping("/batch-delete")
    public JsonResult<?> deleteEntityMapping(@RequestBody List<String> ids) throws Exception {
        boolean success = stockDailyInfoService.deleteEntities(ids);
        log.debug("批量删除数据 股票每日信息:{} {}", ids, success? "成功":"失败");
        return JsonResult.OK();
    }
}