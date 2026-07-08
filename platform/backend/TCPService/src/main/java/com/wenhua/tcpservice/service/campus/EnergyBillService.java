package com.wenhua.tcpservice.service.campus;

import com.wenhua.tcpservice.mapper.CampusEnergyMapper;
import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.campus.EnergyBill;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.service.campus.BillCalculator.CalculationResult;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service @Slf4j @RequiredArgsConstructor
public class EnergyBillService
{
    private final EnvMapper envMapper;
    private final CampusEnergyMapper campusEnergyMapper;

    @Value("${electricity.price:0.6}")
    private double electricityPrice;

    @Value("${lamp.default-power-watt:150}")
    private double defaultLampPowerWatt;

    @Nonnull @Transactional(rollbackFor = Exception.class)
    public EnergyBill generateMonthlyBill(@Nonnull String areaId, @Nonnull String billMonth)
    {
        final var devices = envMapper.selectAllDevice();
        final var result = BillCalculator.calculate(devices, billMonth, electricityPrice, defaultLampPowerWatt);

        final var bill = newBill(billMonth, result);

        campusEnergyMapper.insertBill(bill);

        log.debug("生成月度能耗账单: {}, 总能耗: {}kWh", billMonth, result.totalConsumption());
        return bill;
    }

    @Nonnull
    public List<EnergyBill> getBillHistory(@Nonnull String areaId, int months)
    {
        final List<EnergyBill> bills = new ArrayList<>();
        final var currentMonth = YearMonth.now();
        final var devices = envMapper.selectAllDevice();

        for(var index = 0; index < months; index++)
        {
            final var targetMonth = currentMonth.minusMonths(index);
            final var billMonth = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            final var result = BillCalculator.calculate(devices, billMonth, electricityPrice, defaultLampPowerWatt);
            final var bill = newBill(billMonth, result);

            campusEnergyMapper.insertBill(bill);
            bills.add(bill);
        }

        return bills;
    }

    @Nonnull
    public Map<String, Object> getEnergyDashboard(@Nonnull String areaId)
    {
        final Map<String, Object> dashboard = new HashMap<>();
        final var devices = envMapper.selectAllDevice();
        final var now = YearMonth.now();

        final var currentResult = BillCalculator.calculate(
            devices, now.format(DateTimeFormatter.ofPattern("yyyy-MM")), electricityPrice, defaultLampPowerWatt
        );
        final var currentBill = newBill(now.format(DateTimeFormatter.ofPattern("yyyy-MM")), currentResult);
        campusEnergyMapper.insertBill(currentBill);
        dashboard.put("currentMonth", currentBill);

        final List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        for(var index = 5; index >= 0; index--)
        {
            final var targetMonth = now.minusMonths(index);
            final var billMonth = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            final var result = BillCalculator.calculate(devices, billMonth, electricityPrice, defaultLampPowerWatt);
            final var bill = newBill(billMonth, result);
            campusEnergyMapper.insertBill(bill);

            final Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", billMonth);
            monthData.put("consumption", bill.getTotalConsumption());
            monthData.put("cost", bill.getTotalCost());
            monthData.put("saved", bill.getSavedConsumption());
            monthData.put("savingRate", bill.getSavingRate());
            monthlyTrend.add(monthData);
        }
        dashboard.put("monthlyTrend", monthlyTrend);

        final Map<String, Double> areaConsumption = new HashMap<>();
        for(final var device: devices)
        {
            final var area = device.getName() != null ? device.getName().split("-")[0] : "未知区域";
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                final var brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                final var powerKw = BillCalculator.getDevicePowerKw(device, defaultLampPowerWatt);
                final var consumption = powerKw * BillCalculator.AVG_DAILY_HOURS * (brightness / 100.0) * 30;
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
        final var devices = envMapper.selectAllDevice();
        final var currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        final var result = BillCalculator.calculate(devices, currentMonth, electricityPrice, defaultLampPowerWatt);
        final var currentBill = newBill(currentMonth, result);
        campusEnergyMapper.insertBill(currentBill);

        comparison.put("baselineConsumption", baselineConsumption);
        comparison.put("actualConsumption", result.totalConsumption());
        comparison.put("savedConsumption", baselineConsumption - result.totalConsumption());
        comparison.put(
            "savingRate",
            baselineConsumption > 0
                ? ((baselineConsumption - result.totalConsumption()) / baselineConsumption) * 100
                : 0
        );
        comparison.put(
            "savedCost",
            (baselineConsumption - result.totalConsumption()) * electricityPrice
        );

        return comparison;
    }

    private double calculateBaselineConsumption()
    {
        final var devices = envMapper.selectAllDevice();
        var baselineTotal = .0;

        for(final var device: devices)
            if("lamp".equals(device.getDeviceType()))
            {
                final var powerKw = BillCalculator.getDevicePowerKw(device, defaultLampPowerWatt);
                baselineTotal += powerKw * BillCalculator.AVG_DAILY_HOURS * 30;
            }

        return baselineTotal;
    }

    private EnergyBill newBill(String billMonth, CalculationResult result)
    {
        return new EnergyBill(
            null, null, null, billMonth,
            result.totalConsumption(), result.totalCost(),
            result.savedConsumption(), result.savedCost(), result.savingRate(),
            result.peakConsumption(), result.offPeakConsumption(),
            result.averageDailyConsumption(),
            null, 1, LocalDateTime.now(),
            result.wasteAnalysis(), result.optimizationSuggestions()
        );
    }
}

/**
 * 用于计算 <u>{@link EnergyBill}</u> 的静态工具类.
 * @implNote 原设计为: <u>{@link EnergyBill}</u> 的计算方法位于 <u>{@link EnergyBillService}</u> 内, 并且被标注为 <u>{@link Transactional}</u>, 然而, 该计算方法也被该类的内部其它方法
 * 调用, <span style="color: f84b4b">这会导致 <u>{@link Transactional}</u> 的事务原子性直接失效.</span> 因此计算逻辑必须要抽取成独立的无状态辅助方法.
 */
final class BillCalculator
{
    private BillCalculator() { throw new IllegalAccessError("Class \"BillCalculator\" is not meant to be instantized!"); }
    
    public static final double AVG_DAILY_HOURS = 10;
    public static final int PEAK_HOUR_START = 18;
    public static final int PEAK_HOUR_END = 22;
    
    public record CalculationResult(
        double totalConsumption,
        double peakConsumption,
        double offPeakConsumption,
        double savedConsumption,
        double totalCost,
        double savedCost,
        double savingRate,
        double averageDailyConsumption,
        int daysInMonth,
        String wasteAnalysis,
        String optimizationSuggestions
    ) { }
    
    @Nonnull
    public static CalculationResult calculate(
        @Nonnull List<Device> devices,
        @Nonnull String billMonth,
        double electricityPrice,
        double defaultLampPowerWatt
    )
    {
        final var yearMonth = YearMonth.parse(billMonth);
        final var daysInMonth = yearMonth.lengthOfMonth();
        
        var totalConsumption = .0;
        var peakConsumption = .0;
        var offPeakConsumption = .0;
        var savedConsumption = .0;
        
        for(final var device: devices)
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                final var brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                final var powerKw = getDevicePowerKw(device, defaultLampPowerWatt);
                
                final var dailyConsumption = powerKw * AVG_DAILY_HOURS * (brightness / 100.);
                final var monthlyConsumption = dailyConsumption * daysInMonth;
                
                totalConsumption += monthlyConsumption;
                
                final var baselineDaily = powerKw * AVG_DAILY_HOURS;
                final var baselineMonthly = baselineDaily * daysInMonth;
                savedConsumption += (baselineMonthly - monthlyConsumption);
                
                final var peakHoursPerDay = Math.min(PEAK_HOUR_END - PEAK_HOUR_START, AVG_DAILY_HOURS);
                final var offPeakHoursPerDay = AVG_DAILY_HOURS - peakHoursPerDay;
                peakConsumption += monthlyConsumption * (peakHoursPerDay / AVG_DAILY_HOURS);
                offPeakConsumption += monthlyConsumption * (offPeakHoursPerDay / AVG_DAILY_HOURS);
            }
        
        final var totalCost = totalConsumption * electricityPrice;
        final var savedCost = savedConsumption * electricityPrice;
        final var savingRate = totalConsumption > 0
            ? (savedConsumption / (totalConsumption + savedConsumption)) * 100
            : 0;
        final var averageDailyConsumption = totalConsumption / daysInMonth;
        
        final var wasteAnalysis = generateWasteAnalysis(devices);
        final var optimizationSuggestions = generateOptimizationSuggestions(
            savingRate, peakConsumption, totalConsumption
        );
        
        return new CalculationResult(
            totalConsumption, peakConsumption, offPeakConsumption, savedConsumption,
            totalCost, savedCost, savingRate, averageDailyConsumption, daysInMonth,
            wasteAnalysis, optimizationSuggestions
        );
    }
    
    public static double getDevicePowerKw(@Nonnull Device device, double defaultLampPowerWatt)
    {
        if(device.getPowerConsumption() != null && device.getPowerConsumption() > 0)
            return device.getPowerConsumption() / 1000.;
        return defaultLampPowerWatt / 1000.;
    }
    
    private static String generateWasteAnalysis(@Nonnull List<Device> devices)
    {
        final var analysis = new StringBuilder();
        
        var highBrightnessCount = 0;
        var lowEfficiencyCount = 0;
        
        for(final var device: devices)
            if(device.getLampStatus() != null && device.getLampStatus() == 1)
            {
                final var brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                
                if(brightness >= 90)
                    highBrightnessCount++;
                
                if(device.getLightIntensity() != null && device.getLightIntensity() > 500 && brightness > 50)
                    lowEfficiencyCount++;
            }
        
        if(highBrightnessCount > devices.size() * 0.5)
            analysis.append(String.format("发现%d盏路灯长期高亮度运行，建议优化调光策略；", highBrightnessCount));
        
        if(lowEfficiencyCount > 0)
            analysis.append(String.format("发现%d盏路灯在光照充足时仍保持较高亮度，存在能源浪费；", lowEfficiencyCount));
        
        if(analysis.isEmpty())
            analysis.append("整体能源利用效率良好，未发现明显浪费现象。");
        
        return analysis.toString();
    }
    
    private static String generateOptimizationSuggestions(double savingRate, double peakConsumption, double totalConsumption)
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
}
