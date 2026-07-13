package com.wenhua.tcpservice.service;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.MonitorRecordMapper;
import com.wenhua.tcpservice.pojo.MonitorRecord;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service @Slf4j @RequiredArgsConstructor
public final class AnalysisService
{
    private final MonitorRecordMapper monitorRecordMapper;

    /**
     * 对指定设备最近 N 条记录进行阈值异常检测。
     * @return 每条异常记录的详情列表
     */
    @Nonnull public List<Map<String, Object>> detectAnomalies(@Nonnull String deviceId, int limit)
    {
        final var params = new HashMap<String, Object>();
        params.put("deviceId", deviceId);
        params.put("offset", 0);
        params.put("pageSize", limit);
        final var records = monitorRecordMapper.selectRecords(params);

        final var anomalies = new ArrayList<Map<String, Object>>();

        for(final var record: records)
        {
            final var violations = checkThresholds(record);
            if(!violations.isEmpty())
            {
                final var entry = new HashMap<String, Object>();
                entry.put("recordId", record.getId());
                entry.put("deviceId", record.getDeviceId());
                entry.put("deviceName", record.getDeviceName());
                entry.put("recordTime", record.getRecordTime().toString());
                entry.put("dataType", record.getDataType());
                entry.put("value", record.getValue());
                entry.put("currentStatus", record.getStatus());
                entry.put("violations", violations);
                anomalies.add(entry);
            }
        }

        return anomalies;
    }

    /**
     * 对单个监测值进行阈值检查.
     */
    @Nonnull public List<String> checkThresholds(@Nonnull MonitorRecord record)
    {
        final var violations = new ArrayList<String>();
        final var value = record.getValue();
        final var type = record.getDataType();

        if(type == null) return violations;

        switch(type)
        {
            case "temperature" ->
            {
                if(value > GlobalConfiguration.ALERT_TEMPERATURE_HIGH)
                    violations.add(String.format("温度超上限: %.1f°C > %.1f°C", value, GlobalConfiguration.ALERT_TEMPERATURE_HIGH));
                else if(value < GlobalConfiguration.ALERT_TEMPERATURE_LOW)
                    violations.add(String.format("温度低于下限: %.1f°C < %.1f°C", value, GlobalConfiguration.ALERT_TEMPERATURE_LOW));
            }
            case "humidity" ->
            {
                if(value > GlobalConfiguration.ALERT_HUMIDITY_HIGH)
                    violations.add(String.format("湿度超上限: %.1f%% > %d%%", value, GlobalConfiguration.ALERT_HUMIDITY_HIGH));
                else if(value < GlobalConfiguration.ALERT_HUMIDITY_LOW)
                    violations.add(String.format("湿度低于下限: %.1f%% < %d%%", value, GlobalConfiguration.ALERT_HUMIDITY_LOW));
            }
            case "lightIntensity", "light" ->
            {
                if(value > GlobalConfiguration.MAX_LIGHT_INTENSITY)
                    violations.add(String.format("光照强度超上限: %.0flux > %dlux", value, GlobalConfiguration.MAX_LIGHT_INTENSITY));
            }
        }

        return violations;
    }

    /**
     * 计算指定设备某数据类型的简单移动平均线 (SMA).
     *
     * @param period 移动平均周期（数据点数）
     */
    @Nonnull public Map<String, Object> calculateMovingAverage(@Nonnull String deviceId, @Nonnull String dataType, int period)
    {
        final var params = new HashMap<String, Object>();
        params.put("deviceId", deviceId);
        params.put("dataType", dataType);
        params.put("offset", 0);
        params.put("pageSize", Math.max(period * 2, 50)); // 取足够数据
        final var records = monitorRecordMapper.selectRecords(params);

        // 按时间升序排列
        records.sort(Comparator.comparing(MonitorRecord::getRecordTime));

        final var rawValues = new ArrayList<Double>();
        final var timestamps = new ArrayList<String>();
        final var smaValues = new ArrayList<Double>();

        for(final var record: records)
        {
            rawValues.add(record.getValue());
            timestamps.add(record.getRecordTime().toString());
        }

        // 计算 SMA
        for(var i = 0; i < rawValues.size(); i++)
        {
            if(i < period - 1)
            {
                smaValues.add(null); // 不足周期的位置填 null
                continue;
            }
            double sum = 0;
            for(var j = i - period + 1; j <= i; j++)
                sum += rawValues.get(j);
            smaValues.add(sum / period);
        }

        final var result = new HashMap<String, Object>();
        result.put("deviceId", deviceId);
        result.put("dataType", dataType);
        result.put("period", period);
        result.put("rawValues", rawValues);
        result.put("smaValues", smaValues);
        result.put("timestamps", timestamps);
        return result;
    }

    /**
     * 趋势分析：环比（与上个月比）和同比（与去年同月比）。
     */
    @Nonnull public Map<String, Object> trendAnalysis(@Nonnull String deviceId, @Nonnull String dataType)
    {
        final var now = YearMonth.now();
        final var formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        final var currentMonth = now.format(formatter);
        final var prevMonth = now.minusMonths(1).format(formatter);
        final var lastYearMonth = now.minusYears(1).format(formatter);

        final var currentAvg = averageForMonth(deviceId, dataType, currentMonth);
        final var prevAvg = averageForMonth(deviceId, dataType, prevMonth);
        final var lastYearAvg = averageForMonth(deviceId, dataType, lastYearMonth);

        final Map<String, Object> result = new HashMap<>();
        result.put("deviceId", deviceId);
        result.put("dataType", dataType);
        result.put("currentMonth", currentMonth);
        result.put("currentAverage", currentAvg);
        result.put("previousMonth", prevMonth);
        result.put("previousAverage", prevAvg);
        result.put("lastYearMonth", lastYearMonth);
        result.put("lastYearAverage", lastYearAvg);

        // 环比变化率
        if(prevAvg > 0)
            result.put("monthOverMonthChange", ((currentAvg - prevAvg) / prevAvg) * 100);
        else
            result.put("monthOverMonthChange", 0);

        // 同比变化率
        if(lastYearAvg > 0)
            result.put("yearOverYearChange", ((currentAvg - lastYearAvg) / lastYearAvg) * 100);
        else
            result.put("yearOverYearChange", 0);

        return result;
    }

    /**
     * 自动检查最近的监测记录，将超阈值的记录标记为 warning/abnormal.
     * 返回本次新标记的异常记录数。
     */
    public int autoFlagAnomalies()
    {
        final Map<String, Object> params = new HashMap<>();
        params.put("offset", 0);
        params.put("pageSize", 200);
        final var records = monitorRecordMapper.selectRecords(params);

        var flagged = 0;
        for(final var record: records)
        {
            final var violations = checkThresholds(record);
            if(!violations.isEmpty() && "normal".equals(record.getStatus()))
            {
                // 创建一条新的异常记录，不修改原记录（原记录不可变）
                final var anomaly = new MonitorRecord(
                    null,
                    record.getDeviceId(),
                    record.getDeviceName(),
                    record.getRecordTime(),
                    record.getDataType(),
                    record.getValue(),
                    violations.size() > 1 ? "abnormal" : "warning",
                    String.join("; ", violations),
                    null
                );
                monitorRecordMapper.insertRecord(anomaly);
                flagged++;
            }
        }

        if(flagged > 0)
            log.info("自动标记了 {} 条异常监测记录", flagged);
        return flagged;
    }

    /**
     * 获取某个设备在指定月份的平均值。
     */
    private double averageForMonth(String deviceId, String dataType, String yearMonth)
    {
        final var start = yearMonth + "-01 00:00:00";
        final var end = YearMonth.parse(yearMonth).atEndOfMonth()
            .atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        final Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        params.put("dataType", dataType);
        params.put("startTime", start);
        params.put("endTime", end);
        params.put("offset", 0);
        params.put("pageSize", 5000);
        final var records = monitorRecordMapper.selectRecords(params);

        if(records.isEmpty()) return 0;

        return records.stream().
            mapToDouble(MonitorRecord::getValue).
            average().
            orElse(0);
    }
}
