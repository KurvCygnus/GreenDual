package com.wenhua.tcpservice.service.campus;

import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.campus.EnergyBill;
import com.wenhua.tcpservice.pojo.dev.Device;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class EnergyBillService
{
    private EnvMapper envMapper;
    
    private static final double ELECTRICITY_PRICE = 0.6;
    private static final double AVG_LAMP_POWER = 0.15;
    private static final double AVG_DAILY_HOURS = 10;
    
    public EnergyBill generateMonthlyBill(String areaId, String billMonth)
    {
        EnergyBill bill = new EnergyBill();
        bill.setBillMonth(billMonth);
        bill.setCreateTime(LocalDateTime.now());
        bill.setStatus(1);
        
        List<Device> devices = envMapper.selectAllDevice();
        
        double totalConsumption = 0;
        double peakConsumption = 0;
        double offPeakConsumption = 0;
        double savedConsumption = 0;
        
        for(Device device: devices)
        {
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                int brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                
                double dailyConsumption = AVG_LAMP_POWER * AVG_DAILY_HOURS * (brightness / 100.0);
                double monthlyConsumption = dailyConsumption * 30;
                
                totalConsumption += monthlyConsumption;
                
                double baselineConsumption = AVG_LAMP_POWER * AVG_DAILY_HOURS * 30;
                savedConsumption += (baselineConsumption - monthlyConsumption);
                
                int hour = LocalDateTime.now().getHour();
                if(hour >= 18 && hour <= 22)
                {
                    peakConsumption += dailyConsumption * 0.4;
                }
                else
                {
                    offPeakConsumption += dailyConsumption * 0.6;
                }
            }
        }
        
        bill.setTotalConsumption(totalConsumption);
        bill.setTotalCost(totalConsumption * ELECTRICITY_PRICE);
        bill.setSavedConsumption(savedConsumption);
        bill.setSavedCost(savedConsumption * ELECTRICITY_PRICE);
        bill.setSavingRate(totalConsumption > 0 ? (savedConsumption / (totalConsumption + savedConsumption)) * 100 : 0);
        bill.setPeakConsumption(peakConsumption);
        bill.setOffPeakConsumption(offPeakConsumption);
        bill.setAverageDailyConsumption(totalConsumption / 30);
        
        bill.setWasteAnalysis(generateWasteAnalysis(devices));
        bill.setOptimizationSuggestions(generateOptimizationSuggestions(bill));
        
        log.debug("生成月度能耗账单: {}, 总能耗: {}kWh", billMonth, totalConsumption);
        return bill;
    }
    
    private String generateWasteAnalysis(List<Device> devices)
    {
        StringBuilder analysis = new StringBuilder();
        
        int highBrightnessCount = 0;
        int lowEfficiencyCount = 0;
        int alwaysOnCount = 0;
        
        for(Device device: devices)
        {
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                int brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                
                if(brightness >= 90)
                {
                    highBrightnessCount++;
                }
                
                if(device.getLightIntensity() != null && device.getLightIntensity() > 500 && brightness > 50)
                {
                    lowEfficiencyCount++;
                }
                
                alwaysOnCount++;
            }
        }
        
        if(highBrightnessCount > devices.size() * 0.5)
        {
            analysis.append("发现").append(highBrightnessCount).append("盏路灯长期高亮度运行，建议优化调光策略；");
        }
        
        if(lowEfficiencyCount > 0)
        {
            analysis.append("发现").append(lowEfficiencyCount).append("盏路灯在光照充足时仍保持较高亮度，存在能源浪费；");
        }
        
        if(analysis.length() == 0)
        {
            analysis.append("整体能源利用效率良好，未发现明显浪费现象。");
        }
        
        return analysis.toString();
    }
    
    private String generateOptimizationSuggestions(EnergyBill bill)
    {
        StringBuilder suggestions = new StringBuilder();
        
        if(bill.getSavingRate() < 20)
        {
            suggestions.append("建议启用AI智能调光功能，预计可提升节能率15-20%；\n");
        }
        
        if(bill.getPeakConsumption() > bill.getTotalConsumption() * 0.5)
        {
            suggestions.append("高峰时段能耗占比较高，建议实施分时段差异化调光；\n");
        }
        
        suggestions.append("建议对老旧高能耗灯具进行LED改造，可进一步降低能耗；\n");
        suggestions.append("建议安装光照传感器和人流传感器，实现精准按需照明；\n");
        suggestions.append("建议定期检查线路和设备，减少因故障导致的能源损耗。");
        
        return suggestions.toString();
    }
    
    public List<EnergyBill> getBillHistory(String areaId, int months)
    {
        List<EnergyBill> bills = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        
        for(int i = 0; i < months; i++)
        {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            String billMonth = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            EnergyBill bill = generateMonthlyBill(areaId, billMonth);
            bills.add(bill);
        }
        
        return bills;
    }
    
    public Map<String, Object> getEnergyDashboard(String areaId)
    {
        Map<String, Object> dashboard = new HashMap<>();
        
        EnergyBill currentBill = generateMonthlyBill(areaId,
            YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        );
        
        dashboard.put("currentMonth", currentBill);
        
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        for(int i = 5; i >= 0; i--)
        {
            YearMonth targetMonth = YearMonth.now().minusMonths(i);
            String billMonth = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            EnergyBill bill = generateMonthlyBill(areaId, billMonth);
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", billMonth);
            monthData.put("consumption", bill.getTotalConsumption());
            monthData.put("cost", bill.getTotalCost());
            monthData.put("saved", bill.getSavedConsumption());
            monthData.put("savingRate", bill.getSavingRate());
            monthlyTrend.add(monthData);
        }
        dashboard.put("monthlyTrend", monthlyTrend);
        
        List<Device> devices = envMapper.selectAllDevice();
        Map<String, Double> areaConsumption = new HashMap<>();
        
        for(Device device: devices)
        {
            String area = device.getName() != null ? device.getName().split("-")[0] : "未知区域";
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                int brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                double consumption = AVG_LAMP_POWER * AVG_DAILY_HOURS * (brightness / 100.0) * 30;
                areaConsumption.merge(area, consumption, Double::sum);
            }
        }
        dashboard.put("areaConsumption", areaConsumption);
        
        double yearlyProjection = currentBill.getTotalCost() * 12;
        double yearlySaving = currentBill.getSavedCost() * 12;
        dashboard.put("yearlyProjection", yearlyProjection);
        dashboard.put("yearlySaving", yearlySaving);
        
        return dashboard;
    }
    
    public Map<String, Object> compareWithBaseline(String areaId)
    {
        Map<String, Object> comparison = new HashMap<>();
        
        double baselineConsumption = calculateBaselineConsumption();
        EnergyBill currentBill = generateMonthlyBill(areaId,
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
        List<Device> devices = envMapper.selectAllDevice();
        int lampCount = 0;
        
        for(Device device: devices)
        {
            if("lamp".equals(device.getDeviceType()))
            {
                lampCount++;
            }
        }
        
        return lampCount * AVG_LAMP_POWER * AVG_DAILY_HOURS * 30;
    }
}
