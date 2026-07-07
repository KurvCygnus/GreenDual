package com.wenhua.tcpservice.service.campus;

import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.campus.EnergyBill;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.utils.StringFormatter;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service @Slf4j @AllArgsConstructor
public final class EnergyBillService
{
    private final EnvMapper envMapper;
    
    private static final double ELECTRICITY_PRICE = 0.6;
    private static final double AVG_LAMP_POWER = 0.15;
    private static final double AVG_DAILY_HOURS = 10;
    
    @Nonnull
    public EnergyBill generateMonthlyBill(@Nonnull String areaId, @Nonnull String billMonth)
    {
        final var devices = envMapper.selectAllDevice();
        
        var totalConsumption = .0;
        var peakConsumption = .0;
        var offPeakConsumption = .0;
        var savedConsumption = .0;

        for(final var device: devices)
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                final var brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                
                final var dailyConsumption = AVG_LAMP_POWER * AVG_DAILY_HOURS * (brightness / 100.);
                final var monthlyConsumption = dailyConsumption * 30;
                
                totalConsumption += monthlyConsumption;
                
                final var baselineConsumption = AVG_LAMP_POWER * AVG_DAILY_HOURS * 30;
                savedConsumption += (baselineConsumption - monthlyConsumption);
                
                final var hour = LocalDateTime.now().getHour();
                if(hour >= 18 && hour <= 22)
                    peakConsumption += dailyConsumption * .4;
                else
                    offPeakConsumption += dailyConsumption * .6;
            }

        final var totalCost = totalConsumption * ELECTRICITY_PRICE;
        final var savedCost = savedConsumption * ELECTRICITY_PRICE;
        final var savingRate = totalConsumption > 0 ? (savedConsumption / (totalConsumption + savedConsumption)) * 100 : 0;
        final var averageDailyConsumption = totalConsumption / 30;

        final var wasteAnalysis = generateWasteAnalysis(devices);

        final var bill = new EnergyBill(
            null, null, null, billMonth,
            totalConsumption, totalCost, savedConsumption, savedCost, savingRate,
            peakConsumption, offPeakConsumption, averageDailyConsumption,
            null, 1, LocalDateTime.now(),
            wasteAnalysis, generateOptimizationSuggestions(savingRate, peakConsumption, totalConsumption)
        );

        log.debug("生成月度能耗账单: {}, 总能耗: {}kWh", billMonth, totalConsumption);
        return bill;
    }
    
    private String generateWasteAnalysis(List<Device> devices)
    {
        final var analysis = new StringBuilder();
        
        var highBrightnessCount = 0;
        var lowEfficiencyCount = 0;
        var alwaysOnCount = 0;
        
        for(final var device: devices)
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                final var brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                
                if(brightness >= 90)
                    highBrightnessCount++;
                
                if(device.getLightIntensity() != null && device.getLightIntensity() > 500 && brightness > 50)
                    lowEfficiencyCount++;
                
                alwaysOnCount++;
            }
        
        if(highBrightnessCount > devices.size() * 0.5)
            analysis.append(StringFormatter.format("发现{}盏路灯长期高亮度运行，建议优化调光策略；", highBrightnessCount));

        if(lowEfficiencyCount > 0)
            analysis.append(StringFormatter.format("发现{}盏路灯在光照充足时仍保持较高亮度，存在能源浪费；", lowEfficiencyCount));
        
        if(analysis.isEmpty())
            analysis.append("整体能源利用效率良好，未发现明显浪费现象。");
        
        return analysis.toString();
    }
    
    private String generateOptimizationSuggestions(double savingRate, double peakConsumption, double totalConsumption)
    {
        final var suggestions = new StringBuilder();

        if(savingRate < 20)
            suggestions.append("建议启用AI智能调光功能，预计可提升节能率15-20%；\n");

        if(peakConsumption > totalConsumption * 0.5)
            suggestions.append("高峰时段能耗占比较高，建议实施分时段差异化调光；\n");
        
        suggestions.append("建议对老旧高能耗灯具进行LED改造，可进一步降低能耗；\n");
        suggestions.append("建议安装光照传感器和人流传感器，实现精准按需照明；\n");
        suggestions.append("建议定期检查线路和设备，减少因故障导致的能源损耗。");
        
        return suggestions.toString();
    }
    
    @Nonnull
    public List<EnergyBill> getBillHistory(@Nonnull String areaId, int months)
    {
        final List<EnergyBill> bills = new ArrayList<>();
        final var currentMonth = YearMonth.now();
        
        for(var i = 0; i < months; i++)
        {
            final var targetMonth = currentMonth.minusMonths(i);
            final var billMonth = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            final var bill = generateMonthlyBill(areaId, billMonth);
            bills.add(bill);
        }
        
        return bills;
    }
    
    @Nonnull
    public Map<String, Object> getEnergyDashboard(@Nonnull String areaId)
    {
        final Map<String, Object> dashboard = new HashMap<>();
        
        final var currentBill = generateMonthlyBill(areaId, YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        
        dashboard.put("currentMonth", currentBill);
        
        final List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        for(var i = 5; i >= 0; i--)
        {
            final var targetMonth = YearMonth.now().minusMonths(i);
            final var billMonth = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            final var bill = generateMonthlyBill(areaId, billMonth);
            
            final Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", billMonth);
            monthData.put("consumption", bill.getTotalConsumption());
            monthData.put("cost", bill.getTotalCost());
            monthData.put("saved", bill.getSavedConsumption());
            monthData.put("savingRate", bill.getSavingRate());
            monthlyTrend.add(monthData);
        }
        dashboard.put("monthlyTrend", monthlyTrend);
        
        final var devices = envMapper.selectAllDevice();
        final Map<String, Double> areaConsumption = new HashMap<>();
        
        for(final var device: devices)
        {
            final var area = device.getName() != null ? device.getName().split("-")[0] : "未知区域";
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                final var brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                final var consumption = AVG_LAMP_POWER * AVG_DAILY_HOURS * (brightness / 100.0) * 30;
                areaConsumption.merge(area, consumption, Double::sum);
            }
        }
        dashboard.put("areaConsumption", areaConsumption);
        
        final var yearlyProjection = currentBill.getTotalCost() * 12;
        final var yearlySaving = currentBill.getSavedCost() * 12;
        dashboard.put("yearlyProjection", yearlyProjection);
        dashboard.put("yearlySaving", yearlySaving);
        
        return dashboard;
    }
    
    @Nonnull
    public Map<String, Object> compareWithBaseline(@Nonnull String areaId)
    {
        final Map<String, Object> comparison = new HashMap<>();
        
        final var baselineConsumption = calculateBaselineConsumption();
        final var currentBill = generateMonthlyBill(areaId,
            YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        );
        
        comparison.put("baselineConsumption", baselineConsumption);
        comparison.put("actualConsumption", currentBill.getTotalConsumption());
        comparison.put("savedConsumption", baselineConsumption - currentBill.getTotalConsumption());
        comparison.put("savingRate", ((baselineConsumption - currentBill.getTotalConsumption()) / baselineConsumption) * 100);
        comparison.put("savedCost", (baselineConsumption - currentBill.getTotalConsumption()) * ELECTRICITY_PRICE);
        
        return comparison;
    }
    
    private double calculateBaselineConsumption()
    {
        final var devices = envMapper.selectAllDevice();
        var lampCount = 0;
        
        for(final var device: devices)
            if("lamp".equals(device.getDeviceType()))
                lampCount++;
        
        return lampCount * AVG_LAMP_POWER * AVG_DAILY_HOURS * 30;
    }
}
