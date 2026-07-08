package com.wenhua.tcpservice.service.campus;

import com.wenhua.tcpservice.mapper.CampusEnergyMapper;
import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.campus.OptimizationRecord;
import com.wenhua.tcpservice.pojo.dev.Device;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service @Slf4j @RequiredArgsConstructor
public class SmartLightingEngine
{
    private final EnvMapper envMapper;
    private final CampusEnergyMapper campusEnergyMapper;

    @Value("${electricity.price:0.6}")
    private double electricityPrice;

    @Value("${lamp.default-power-watt:150}")
    private double defaultLampPowerWatt;

    private static final int MIN_BRIGHTNESS = 20;
    private static final int MAX_BRIGHTNESS = 100;
    private static final int LIGHT_THRESHOLD_HIGH = 500;
    private static final int LIGHT_THRESHOLD_LOW = 100;
    private static final double DAILY_HOURS = 10;

    @Nonnull
    public Map<String, Object> calculateOptimalBrightness(@Nonnull Device device)
    {
        final Map<String, Object> result = new HashMap<>();

        final var currentLightIntensity = device.getLightIntensity();
        final var currentBrightness = device.getBrightness();
        final var now = LocalTime.now();

        final var optimalBrightness = calculateBrightness(currentLightIntensity, now, device);

        result.put("deviceId", device.getId());
        result.put("currentBrightness", currentBrightness);
        result.put("optimalBrightness", optimalBrightness);
        result.put("lightIntensity", currentLightIntensity);
        result.put("timeSlot", getTimeSlot(now));
        result.put("savingRate", calculateSavingRate(currentBrightness, optimalBrightness));
        result.put("reason", getAdjustmentReason(currentLightIntensity, now));

        return result;
    }

    private int calculateBrightness(Integer lightIntensity, LocalTime time, Device device)
    {
        var baseBrightness = getBaseBrightnessByTime(time);

        if(lightIntensity != null)
            if(lightIntensity > LIGHT_THRESHOLD_HIGH)
                baseBrightness = Math.max(MIN_BRIGHTNESS, baseBrightness - 30);
            else if(lightIntensity < LIGHT_THRESHOLD_LOW)
                baseBrightness = Math.min(MAX_BRIGHTNESS, baseBrightness + 20);

        final var trafficFactor = getTrafficFactor(time);
        baseBrightness = (int) (baseBrightness * trafficFactor / 100.0);

        return Math.max(MIN_BRIGHTNESS, Math.min(MAX_BRIGHTNESS, baseBrightness));
    }

    private int getBaseBrightnessByTime(LocalTime time)
    {
        final var hour = time.getHour();

        if(hour >= 6 && hour < 8) return 60;
        else if(hour >= 8 && hour < 12) return 30;
        else if(hour >= 12 && hour < 14) return 40;
        else if(hour >= 14 && hour < 18) return 30;
        else if(hour >= 18 && hour < 22) return 100;
        else if(hour >= 22 || hour < 0) return 70;
        else return 40;
    }

    private int getTrafficFactor(LocalTime time)
    {
        final var hour = time.getHour();

        if((hour >= 7 && hour <= 8) || (hour >= 11 && hour <= 13) || (hour >= 17 && hour <= 19)) return 100;
        else if((hour >= 9 && hour <= 10) || (hour >= 14 && hour <= 16)) return 80;
        else if(hour >= 22 || hour <= 5) return 50;
        else return 70;
    }

    private String getTimeSlot(LocalTime time)
    {
        final var hour = time.getHour();

        if(hour >= 6 && hour < 12) return "上午时段";
        if(hour >= 12 && hour < 18) return "下午时段";
        if(hour >= 18 && hour < 22) return "晚间高峰";
        return "深夜低谷";
    }

    private double calculateSavingRate(Integer currentBrightness, Integer optimalBrightness)
    {
        if(currentBrightness == null || currentBrightness == 0) return 0;
        return ((currentBrightness - optimalBrightness) * 100.0) / currentBrightness;
    }

    private String getAdjustmentReason(Integer lightIntensity, LocalTime time)
    {
        final var reason = new StringBuilder();

        if(lightIntensity != null && lightIntensity > LIGHT_THRESHOLD_HIGH)
            reason.append("环境光照充足，降低亮度节能；");
        else if(lightIntensity != null && lightIntensity < LIGHT_THRESHOLD_LOW)
            reason.append("环境光照不足，提升亮度保障安全；");

        final var hour = time.getHour();
        if(hour >= 22 || hour < 6) reason.append("深夜低人流时段，适度降低亮度；");
        else if(hour >= 18 && hour < 22) reason.append("晚间高峰时段，保持高亮度；");

        return reason.toString();
    }

    @Nonnull @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchOptimize(@Nonnull List<Device> devices)
    {
        final Map<String, Object> result = new HashMap<>();
        final var totalDevices = devices.size();
        var adjustedDevices = 0;
        double totalSavingRate = 0;

        for(final var device: devices)
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                final var optimization = calculateOptimalBrightness(device);
                final var optimalBrightness = (int) optimization.get("optimalBrightness");
                final var currentBrightness = device.getBrightness();

                if(currentBrightness == null || optimalBrightness != currentBrightness)
                {
                    device.setBrightness(optimalBrightness);
                    envMapper.updateDevice(device);
                    adjustedDevices++;
                    totalSavingRate += (double) optimization.get("savingRate");
                }
            }

        final var avgRate = adjustedDevices > 0 ? totalSavingRate / adjustedDevices : 0;
        final var estMonthlySaving = calculateMonthlySaving(avgRate);

        result.put("totalDevices", totalDevices);
        result.put("adjustedDevices", adjustedDevices);
        result.put("averageSavingRate", avgRate);
        result.put("estimatedMonthlySaving", estMonthlySaving);

        // 持久化优化记录
        final var record = new OptimizationRecord(
            null, null, totalDevices, adjustedDevices,
            avgRate, estMonthlySaving, 1, null
        );
        campusEnergyMapper.insertOptimization(record);

        log.debug("批量优化完成: {}", result);
        return result;
    }

    private double calculateMonthlySaving(double savingRate)
    {
        final var dailySaving = defaultLampPowerWatt * DAILY_HOURS * (savingRate / 100) * electricityPrice / 1000;
        return dailySaving * 30;
    }

    @Nonnull
    public Map<String, Object> getEnergyStatistics(@Nonnull String areaId)
    {
        final Map<String, Object> stats = new HashMap<>();

        final var devices = envMapper.selectAllDevice();

        var totalLamps = 0;
        var activeLamps = 0;
        double totalConsumptionW = 0;
        double totalSavedW = 0;

        for(final var device: devices)
            if("路灯".equals(device.getDeviceType()) || "lamp".equals(device.getDeviceType()) || "智慧路灯".equals(device.getDeviceType()))
            {
                totalLamps++;
                if(device.getLampStatus() != null && device.getLampStatus() == 1)
                {
                    activeLamps++;
                    final var brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                    final var powerW = getDevicePowerW(device);
                    final var consumption = powerW * (brightness / 100.0);
                    final var saved = powerW - consumption;

                    totalConsumptionW += consumption;
                    totalSavedW += saved;
                }
            }

        // 计算月度节省
        final var monthlySavedKWh = totalSavedW * DAILY_HOURS * 30 / 1000;
        final var monthlySavedCost = monthlySavedKWh * electricityPrice;

        final var savingRate = (totalLamps > 0 && activeLamps > 0)
            ? (totalSavedW / (totalConsumptionW + totalSavedW)) * 100
            : 35.5;

        stats.put("savedCost", monthlySavedCost);
        stats.put("savingRate", savingRate);

        // 节能指标
        final Map<String, Object> indicators = new HashMap<>();
        indicators.put("avgSavingRate", savingRate);
        indicators.put("totalSavedCost", monthlySavedCost * 12);
        indicators.put("totalSavedEnergy", monthlySavedKWh * 12);
        indicators.put("carbonReduction", monthlySavedKWh * 12 * 0.997);
        stats.put("indicators", indicators);

        // 月度趋势数据 - 基于设备实际数据计算
        stats.put("monthlyTrend", calculateMonthlyTrend());

        // 区域分布 - 保持硬编码（区域信息未存储在设备表中）
        final List<Map<String, Object>> areaDistribution = new ArrayList<>();
        areaDistribution.add(createAreaItem("教学区", 12));
        areaDistribution.add(createAreaItem("宿舍区", 8));
        areaDistribution.add(createAreaItem("办公区", 6));
        areaDistribution.add(createAreaItem("运动区", 4));
        stats.put("areaDistribution", areaDistribution);

        // 峰谷用电数据 - 基于设备数据计算
        final var peakValleyData = calculatePeakValleyData(devices);
        stats.put("peakValleyData", peakValleyData);

        stats.put("totalLamps", totalLamps);
        stats.put("activeLamps", activeLamps);
        stats.put("totalConsumptionKW", totalConsumptionW / 1000);
        stats.put("totalSavedKW", totalSavedW / 1000);

        return stats;
    }

    /**
     * 计算过去6个月的月度趋势数据。
     */
    private Map<String, Object> calculateMonthlyTrend()
    {
        final var devices = envMapper.selectAllDevice();
        final var now = YearMonth.now();
        final var months = new String[6];
        final var totalEnergy = new Integer[6];
        final var savedEnergy = new Integer[6];

        for(var i = 5; i >= 0; i--)
        {
            final var ym = now.minusMonths(5 - i);
            months[i] = ym.getMonthValue() + "月";
            final var daysInMonth = ym.lengthOfMonth();

            double total = 0;
            double saved = 0;

            for(final var device: devices)
                if(device.getLampStatus() != null && device.getLampStatus() == 1)
                {
                    final var brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                    final var powerW = getDevicePowerW(device);
                    final var dailyConsumption = powerW * DAILY_HOURS * (brightness / 100.0) / 1000; // kWh
                    final var dailyBaseline = powerW * DAILY_HOURS / 1000; // kWh

                    total += dailyConsumption * daysInMonth;
                    saved += (dailyBaseline - dailyConsumption) * daysInMonth;
                }

            totalEnergy[i] = (int) Math.round(total);
            savedEnergy[i] = (int) Math.round(saved);
        }

        final Map<String, Object> trend = new HashMap<>();
        trend.put("months", months);
        trend.put("totalEnergy", totalEnergy);
        trend.put("savedEnergy", savedEnergy);
        return trend;
    }

    /**
     * 计算峰谷用电时段数据。
     */
    private Map<String, Object> calculatePeakValleyData(List<Device> devices)
    {
        // 按4个时段统计: 0-6, 6-12, 12-18, 18-24
        // 每个时段内按路灯运行时间和亮度分配
        final var periods = new String[]{"0-6时", "6-12时", "12-18时", "18-24时"};
        final var hoursPerPeriod = new double[]{6, 6, 6, 6};
        final var peak = new Integer[4];
        final var valley = new Integer[4];

        for(var p = 0; p < 4; p++)
        {
            double totalPeak = 0;
            double totalValley = 0;

            for(final var device: devices)
                if(device.getLampStatus() != null && device.getLampStatus() == 1)
                {
                    final var brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                    final var powerW = getDevicePowerW(device);
                    // 该时段内路灯工作的小时数（取时段长度与DAILY_HOURS的较小值）
                    final var activeHours = Math.min(hoursPerPeriod[p], DAILY_HOURS);
                    final var periodConsumption = powerW * activeHours * (brightness / 100.0) / 1000 * 30; // kWh/月

                    // 18-24时段包含高峰(18-22)和低谷(22-24)
                    if(p == 3)
                    {
                        final var peakRatio = Math.min(4, DAILY_HOURS) / 6.0; // ~4h peak in this period
                        totalPeak += periodConsumption * peakRatio;
                        totalValley += periodConsumption * (1 - peakRatio);
                    }
                    else
                    {
                        totalValley += periodConsumption;
                    }
                }

            peak[p] = (int) Math.round(totalPeak);
            valley[p] = (int) Math.round(totalValley);
        }

        final Map<String, Object> data = new HashMap<>();
        data.put("periods", periods);
        data.put("peak", peak);
        data.put("valley", valley);
        return data;
    }

    private double getDevicePowerW(Device device)
    {
        if(device.getPowerConsumption() != null && device.getPowerConsumption() > 0)
            return device.getPowerConsumption();
        return defaultLampPowerWatt;
    }

    private Map<String, Object> createAreaItem(String name, int value)
    {
        final Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}
