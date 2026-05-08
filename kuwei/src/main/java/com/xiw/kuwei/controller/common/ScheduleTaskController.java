package com.xiw.kuwei.controller.common;

import com.diboot.core.controller.BaseCrudRestController;
import com.diboot.core.vo.JsonResult;
import com.diboot.core.vo.Pagination;
import com.diboot.iam.annotation.BindPermission;
import com.diboot.iam.annotation.Log;
import com.diboot.iam.annotation.OperationCons;
import com.xiw.kuwei.dto.common.ScheduleTaskDTO;
import com.xiw.kuwei.entity.common.ScheduleTask;
import com.xiw.kuwei.service.common.ScheduleTaskService;
import com.xiw.kuwei.vo.common.ScheduleTaskDetailVO;
import com.xiw.kuwei.vo.common.ScheduleTaskListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
* 定时任务 相关rest接口
* @author xiw
* @version 1.0
* @date 2026-05-08
* Copyright © xiwwwwww
*/
@Slf4j
@RequestMapping("/common/schedule-task")
@BindPermission(name = "定时任务")
@RestController
public class ScheduleTaskController extends BaseCrudRestController<ScheduleTask> {

    @Autowired
    private ScheduleTaskService scheduleTaskService;

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
    public JsonResult<List<ScheduleTaskListVO>> getListVOMapping(ScheduleTaskDTO queryDto, Pagination pagination) throws Exception {
        return super.getViewObjectList(queryDto, pagination, ScheduleTaskListVO.class);
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
    public JsonResult<ScheduleTaskDetailVO> getDetailVOMapping(@PathVariable("id")String id) throws Exception {
        ScheduleTaskDetailVO detailVO = scheduleTaskService.getViewObject(id, ScheduleTaskDetailVO.class);
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
    public JsonResult<ScheduleTaskDetailVO> getDetailVOMapping(@PathVariable("fieldName") String fieldName, @PathVariable("fieldValue") String fieldValue) throws Exception {
        ScheduleTaskDetailVO detailVO = scheduleTaskService.getViewObject(fieldName, fieldValue, ScheduleTaskDetailVO.class);
        return JsonResult.OK(detailVO);
    }


    /**
    * 创建资源对象数据
    * @param scheduleTask
    * @return JsonResult
    * @throws Exception
    */
    @Log(operation = OperationCons.LABEL_CREATE)
    @BindPermission(name = OperationCons.LABEL_CREATE, code = OperationCons.CODE_WRITE)
    @PostMapping()
    public JsonResult<Serializable> createModelMapping(@RequestBody ScheduleTask scheduleTask) throws Exception {
        scheduleTaskService.createEntity(scheduleTask);
        return JsonResult.OK(scheduleTask.getId());
    }

    /**
    * 根据id更新资源对象
    * @param scheduleTask
    * @return JsonResult
    * @throws Exception
    */
    @Log(operation = OperationCons.LABEL_UPDATE)
    @BindPermission(name = OperationCons.LABEL_UPDATE, code = OperationCons.CODE_WRITE)
    @PutMapping("/{id}")
    public JsonResult<?> updateModelMapping(@PathVariable("id")String id, @RequestBody ScheduleTask scheduleTask) throws Exception {
        scheduleTaskService.updateEntity(scheduleTask);
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
        boolean success = scheduleTaskService.deleteEntity(id);
        log.debug("删除数据 定时任务:{} {}", id, success? "成功":"失败");
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
        boolean success = scheduleTaskService.deleteEntities(ids);
        log.debug("批量删除数据 定时任务:{} {}", ids, success? "成功":"失败");
        return JsonResult.OK();
    }
}