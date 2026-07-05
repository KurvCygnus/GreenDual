package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.MonitorRecordMapper;
import com.wenhua.tcpservice.pojo.MonitorRecord;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.utils.StringFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@RequestMapping(GlobalConfiguration.USER_REQUEST_PREFIX + "/record")
@AllArgsConstructor
@Slf4j
@Tag(name = "监测记录", description = "环境监测记录查询、统计接口")
public final class MonitorRecordController {

    private MonitorRecordMapper monitorRecordMapper;

    @Operation(summary = "获取监测记录列表")
    @PostMapping("/list")
    public Result getRecordList(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            int page = params.get("page") != null ? (Integer) params.get("page") : 1;
            int pageSize = params.get("pageSize") != null ? (Integer) params.get("pageSize") : 10;
            String deviceId = (String) params.get("deviceId");
            String dataType = (String) params.get("dataType");
            List<String> dateRange = (List<String>) params.get("dateRange");

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("deviceId", deviceId);
            queryParams.put("dataType", dataType);
            queryParams.put("offset", (page - 1) * pageSize);
            queryParams.put("pageSize", pageSize);

            if (dateRange != null && dateRange.size() == 2) {
                queryParams.put("startTime", dateRange.get(0) + " 00:00:00");
                queryParams.put("endTime", dateRange.get(1) + " 23:59:59");
            }

            List<MonitorRecord> records = monitorRecordMapper.selectRecords(queryParams);
            int total = monitorRecordMapper.countRecords(queryParams);

            return Result.success(records).totalPages(total);
        } catch (Exception e) {
            log.error("获取监测记录失败: {}", e.getMessage());
            return Result.error("获取监测记录失败");
        }
    }

    @Operation(summary = "获取监测记录统计")
    @PostMapping("/stats")
    public Result getRecordStats(HttpServletRequest request) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", monitorRecordMapper.countRecords(new HashMap<>()));
            stats.put("normal", monitorRecordMapper.countByStatus("normal"));
            stats.put("warning", monitorRecordMapper.countByStatus("warning"));
            stats.put("abnormal", monitorRecordMapper.countByStatus("abnormal"));
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage());
            return Result.error("获取统计信息失败");
        }
    }

    @Operation(summary = "获取监测记录详情")
    @PostMapping("/detail")
    public Result getRecordDetail(@RequestBody Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            MonitorRecord record = monitorRecordMapper.selectById(id);
            if (record != null) {
                return Result.success(record);
            }
            return Result.error("记录不存在");
        } catch (Exception e) {
            log.error("获取记录详情失败: {}", e.getMessage());
            return Result.error("获取记录详情失败");
        }
    }

    @Operation(summary = "删除监测记录")
    @PostMapping("/delete")
    public Result deleteRecord(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Integer id = (Integer) params.get("id");
            monitorRecordMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除记录失败: {}", e.getMessage());
            return Result.error("删除记录失败");
        }
    }

    @Operation(summary = "添加监测记录")
    @PostMapping("/add")
    public Result addRecord(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String recordTimeStr = (String) params.get("recordTime");
            LocalDateTime recordTime;
            if (recordTimeStr != null && !recordTimeStr.isEmpty()) {
                try {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    recordTime = LocalDateTime.parse(recordTimeStr, formatter);
                } catch (Exception e) {
                    recordTime = LocalDateTime.now();
                }
            } else {
                recordTime = LocalDateTime.now();
            }

            String status = (String) params.get("status");
            if (status == null) status = "normal";

            MonitorRecord record = new MonitorRecord(
                    null,
                    (String) params.get("deviceId"),
                    (String) params.get("deviceName"),
                    recordTime,
                    (String) params.get("dataType"),
                    params.get("value") != null ? Double.parseDouble(params.get("value").toString()) : 0.0,
                    status,
                    (String) params.get("remark"),
                    null
            );
            monitorRecordMapper.insertRecord(record);
            return Result.success("添加成功");
        } catch (Exception e) {
            log.error("添加记录失败: {}", e.getMessage());
            return Result.error(StringFormatter.format("添加记录失败: {}", e.getMessage()));
        }
    }
}
