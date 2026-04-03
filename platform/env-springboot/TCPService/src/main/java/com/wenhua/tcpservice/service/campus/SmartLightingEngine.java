package com.wenhua.tcpservice.service.campus;

import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SmartLightingEngine {

    @Autowired
    private EnvMapper envMapper;

    private static final int MIN_BRIGHTNESS = 20;
    private static final int MAX_BRIGHTNESS = 100;
    private static final int LIGHT_THRESHOLD_HIGH = 500;
    private static final int LIGHT_THRESHOLD_LOW = 100;

    public Map<String, Object> calculateOptimalBrightness(Device device) {
        Map<String, Object> result = new HashMap<>();
        
        Integer currentLightIntensity = device.getLightIntensity();
        Integer currentBrightness = device.getBrightness();
        LocalTime now = LocalTime.now();
        
        int optimalBrightness = calculateBrightness(currentLightIntensity, now, device);
        
        result.put("deviceId", device.getId());
        result.put("currentBrightness", currentBrightness);
        result.put("optimalBrightness", optimalBrightness);
        result.put("lightIntensity", currentLightIntensity);
        result.put("timeSlot", getTimeSlot(now));
        result.put("savingRate", calculateSavingRate(currentBrightness, optimalBrightness));
        result.put("reason", getAdjustmentReason(currentLightIntensity, now));
        
        return result;
    }

    private int calculateBrightness(Integer lightIntensity, LocalTime time, Device device) {
        int baseBrightness = getBaseBrightnessByTime(time);
        
        if (lightIntensity != null) {
            if (lightIntensity > LIGHT_THRESHOLD_HIGH) {
                baseBrightness = Math.max(MIN_BRIGHTNESS, baseBrightness - 30);
            } else if (lightIntensity < LIGHT_THRESHOLD_LOW) {
                baseBrightness = Math.min(MAX_BRIGHTNESS, baseBrightness + 20);
            }
        }
        
        int trafficFactor = getTrafficFactor(time);
        baseBrightness = (int) (baseBrightness * trafficFactor / 100.0);
        
        return Math.max(MIN_BRIGHTNESS, Math.min(MAX_BRIGHTNESS, baseBrightness));
    }

    private int getBaseBrightnessByTime(LocalTime time) {
        int hour = time.getHour();
        
        if (hour >= 6 && hour < 8) {
            return 60;
        } else if (hour >= 8 && hour < 12) {
            return 30;
        } else if (hour >= 12 && hour < 14) {
            return 40;
        } else if (hour >= 14 && hour < 18) {
            return 30;
        } else if (hour >= 18 && hour < 22) {
            return 100;
        } else if (hour >= 22 || hour < 0) {
            return 70;
        } else {
            return 40;
        }
    }

    private int getTrafficFactor(LocalTime time) {
        int hour = time.getHour();
        
        if ((hour >= 7 && hour <= 8) || (hour >= 11 && hour <= 13) || (hour >= 17 && hour <= 19)) {
            return 100;
        } else if ((hour >= 9 && hour <= 10) || (hour >= 14 && hour <= 16)) {
            return 80;
        } else if (hour >= 22 || hour <= 5) {
            return 50;
        } else {
            return 70;
        }
    }

    private String getTimeSlot(LocalTime time) {
        int hour = time.getHour();
        
        if (hour >= 6 && hour < 12) return "上午时段";
        if (hour >= 12 && hour < 18) return "下午时段";
        if (hour >= 18 && hour < 22) return "晚间高峰";
        return "深夜低谷";
    }

    private double calculateSavingRate(Integer currentBrightness, Integer optimalBrightness) {
        if (currentBrightness == null || currentBrightness == 0) return 0;
        return ((currentBrightness - optimalBrightness) * 100.0) / currentBrightness;
    }

    private String getAdjustmentReason(Integer lightIntensity, LocalTime time) {
        StringBuilder reason = new StringBuilder();
        
        if (lightIntensity != null && lightIntensity > LIGHT_THRESHOLD_HIGH) {
            reason.append("环境光照充足，降低亮度节能；");
        } else if (lightIntensity != null && lightIntensity < LIGHT_THRESHOLD_LOW) {
            reason.append("环境光照不足，提升亮度保障安全；");
        }
        
        int hour = time.getHour();
        if (hour >= 22 || hour < 6) {
            reason.append("深夜低人流时段，适度降低亮度；");
        } else if (hour >= 18 && hour < 22) {
            reason.append("晚间高峰时段，保持高亮度；");
        }
        
        return reason.toString();
    }

    public Map<String, Object> batchOptimize(List<Device> devices) {
        Map<String, Object> result = new HashMap<>();
        int totalDevices = devices.size();
        int adjustedDevices = 0;
        double totalSavingRate = 0;
        
        for (Device device : devices) {
            if (device.getLampStatus() != null && device.getLampStatus() == 1) {
                Map<String, Object> optimization = calculateOptimalBrightness(device);
                int optimalBrightness = (int) optimization.get("optimalBrightness");
                Integer currentBrightness = device.getBrightness();
                
                if (currentBrightness == null || optimalBrightness != currentBrightness) {
                    device.setBrightness(optimalBrightness);
                    envMapper.updateDevice(device);
                    adjustedDevices++;
                    totalSavingRate += (double) optimization.get("savingRate");
                }
            }
        }
        
        result.put("totalDevices", totalDevices);
        result.put("adjustedDevices", adjustedDevices);
        result.put("averageSavingRate", adjustedDevices > 0 ? totalSavingRate / adjustedDevices : 0);
        result.put("estimatedMonthlySaving", calculateMonthlySaving(totalSavingRate / Math.max(adjustedDevices, 1)));
        
        Log.d("批量优化完成: " + result);
        return result;
    }

    private double calculateMonthlySaving(double savingRate) {
        double avgLampPower = 150;
        double avgDailyHours = 10;
        double electricityPrice = 0.6;
        
        double dailySaving = avgLampPower * avgDailyHours * (savingRate / 100) * electricityPrice / 1000;
        return dailySaving * 30;
    }

    public Map<String, Object> getEnergyStatistics(String areaId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Device> devices = envMapper.selectAllDevice();
        
        int totalLamps = 0;
        int activeLamps = 0;
        double totalConsumption = 0;
        double totalSaved = 0;
        
        for (Device device : devices) {
            if ("lamp".equals(device.getDeviceType())) {
                totalLamps++;
                if (device.getLampStatus() != null && device.getLampStatus() == 1) {
                    activeLamps++;
                    int brightness = device.getBrightness() != null ? device.getBrightness() : 100;
                    totalConsumption += 150 * (brightness / 100.0);
                    totalSaved += 150 * (1 - brightness / 100.0);
                }
            }
        }
        
        stats.put("totalLamps", totalLamps);
        stats.put("activeLamps", activeLamps);
        stats.put("totalConsumptionKW", totalConsumption / 1000);
        stats.put("totalSavedKW", totalSaved / 1000);
        stats.put("savingRate", totalConsumption > 0 ? (totalSaved / (totalConsumption + totalSaved)) * 100 : 0);
        
        return stats;
    }
}
