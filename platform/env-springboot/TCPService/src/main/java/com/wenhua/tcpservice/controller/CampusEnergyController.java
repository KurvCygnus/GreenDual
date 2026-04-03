package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.pojo.campus.EnergyBill;
import com.wenhua.tcpservice.pojo.campus.TeachingCase;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.service.campus.EnergyBillService;
import com.wenhua.tcpservice.service.campus.SmartLightingEngine;
import com.wenhua.tcpservice.service.campus.TeachingCaseService;
import com.wenhua.tcpservice.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@RequestMapping("/campus")
public class CampusEnergyController {

    @Autowired
    private SmartLightingEngine smartLightingEngine;

    @Autowired
    private EnergyBillService energyBillService;

    @Autowired
    private TeachingCaseService teachingCaseService;

    @Autowired
    private EnvMapper envMapper;

    @PostMapping("/lighting/optimize")
    public Result optimizeLighting(@RequestBody Map<String, Object> params) {
        try {
            String deviceId = (String) params.get("deviceId");
            
            if (deviceId != null) {
                Device device = envMapper.selectDeviceById(deviceId);
                if (device == null) {
                    return Result.error("设备不存在");
                }
                Map<String, Object> result = smartLightingEngine.calculateOptimalBrightness(device);
                return Result.success(result);
            } else {
                List<Device> devices = envMapper.selectAllDevice();
                Map<String, Object> result = smartLightingEngine.batchOptimize(devices);
                return Result.success(result);
            }
        } catch (Exception e) {
            Log.e("智能调光失败: " + e.getMessage());
            return Result.error("智能调光失败: " + e.getMessage());
        }
    }

    @PostMapping("/lighting/apply")
    public Result applyOptimization(@RequestBody Map<String, Object> params) {
        try {
            String deviceId = (String) params.get("deviceId");
            Integer brightness = (Integer) params.get("brightness");
            
            if (deviceId == null || brightness == null) {
                return Result.error("参数错误");
            }
            
            Device device = envMapper.selectDeviceById(deviceId);
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            device.setBrightness(brightness);
            if (brightness > 0) {
                device.setLampStatus(1);
            } else {
                device.setLampStatus(0);
            }
            
            envMapper.updateDevice(device);
            
            Log.d("应用优化方案成功: " + deviceId + " -> " + brightness + "%");
            return Result.success("优化方案应用成功");
        } catch (Exception e) {
            Log.e("应用优化方案失败: " + e.getMessage());
            return Result.error("应用优化方案失败: " + e.getMessage());
        }
    }

    @GetMapping("/energy/statistics")
    public Result getEnergyStatistics(@RequestParam(required = false) String areaId) {
        try {
            Map<String, Object> stats = smartLightingEngine.getEnergyStatistics(areaId);
            return Result.success(stats);
        } catch (Exception e) {
            Log.e("获取能源统计失败: " + e.getMessage());
            return Result.error("获取能源统计失败: " + e.getMessage());
        }
    }

    @PostMapping("/energy/bill/generate")
    public Result generateBill(@RequestBody Map<String, Object> params) {
        try {
            String areaId = (String) params.get("areaId");
            String billMonth = (String) params.get("billMonth");
            
            EnergyBill bill = energyBillService.generateMonthlyBill(areaId, billMonth);
            return Result.success(bill);
        } catch (Exception e) {
            Log.e("生成能耗账单失败: " + e.getMessage());
            return Result.error("生成能耗账单失败: " + e.getMessage());
        }
    }

    @GetMapping("/energy/bill/history")
    public Result getBillHistory(@RequestParam(required = false) String areaId,
                                  @RequestParam(defaultValue = "6") int months) {
        try {
            List<EnergyBill> bills = energyBillService.getBillHistory(areaId, months);
            return Result.success(bills);
        } catch (Exception e) {
            Log.e("获取账单历史失败: " + e.getMessage());
            return Result.error("获取账单历史失败: " + e.getMessage());
        }
    }

    @GetMapping("/energy/dashboard")
    public Result getEnergyDashboard(@RequestParam(required = false) String areaId) {
        try {
            Map<String, Object> dashboard = energyBillService.getEnergyDashboard(areaId);
            return Result.success(dashboard);
        } catch (Exception e) {
            Log.e("获取能源仪表盘失败: " + e.getMessage());
            return Result.error("获取能源仪表盘失败: " + e.getMessage());
        }
    }

    @GetMapping("/energy/compare")
    public Result compareWithBaseline(@RequestParam(required = false) String areaId) {
        try {
            Map<String, Object> comparison = energyBillService.compareWithBaseline(areaId);
            return Result.success(comparison);
        } catch (Exception e) {
            Log.e("能耗对比失败: " + e.getMessage());
            return Result.error("能耗对比失败: " + e.getMessage());
        }
    }

    @PostMapping("/teaching/case/create")
    public Result createTeachingCase(@RequestBody Map<String, Object> params) {
        try {
            String deviceId = (String) params.get("deviceId");
            String caseType = (String) params.get("caseType");
            
            if (deviceId == null || caseType == null) {
                return Result.error("参数错误");
            }
            
            Device device = envMapper.selectDeviceById(deviceId);
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            TeachingCase teachingCase = teachingCaseService.createTeachingCase(device, caseType);
            return Result.success(teachingCase);
        } catch (Exception e) {
            Log.e("创建教学案例失败: " + e.getMessage());
            return Result.error("创建教学案例失败: " + e.getMessage());
        }
    }

    @GetMapping("/teaching/cases")
    public Result getTeachingCases(@RequestParam(required = false) String course) {
        try {
            if (course != null && !course.isEmpty()) {
                List<TeachingCase> cases = teachingCaseService.getTeachingCasesByCourse(course);
                return Result.success(cases);
            } else {
                Map<String, Object> stats = teachingCaseService.getTeachingStatistics();
                return Result.success(stats);
            }
        } catch (Exception e) {
            Log.e("获取教学案例失败: " + e.getMessage());
            return Result.error("获取教学案例失败: " + e.getMessage());
        }
    }

    @GetMapping("/teaching/statistics")
    public Result getTeachingStatistics() {
        try {
            Map<String, Object> stats = teachingCaseService.getTeachingStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            Log.e("获取教学统计失败: " + e.getMessage());
            return Result.error("获取教学统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/overview")
    public Result getCampusOverview() {
        try {
            Map<String, Object> overview = new java.util.HashMap<>();
            
            Map<String, Object> energyStats = smartLightingEngine.getEnergyStatistics(null);
            overview.put("energy", energyStats);
            
            Map<String, Object> dashboard = energyBillService.getEnergyDashboard(null);
            overview.put("dashboard", dashboard);
            
            Map<String, Object> teachingStats = teachingCaseService.getTeachingStatistics();
            overview.put("teaching", teachingStats);
            
            List<Device> devices = envMapper.selectAllDevice();
            overview.put("totalDevices", devices.size());
            
            int onlineDevices = 0;
            int lampCount = 0;
            for (Device device : devices) {
                if (device.getOnLine() != null && device.getOnLine() == 1) {
                    onlineDevices++;
                }
                if ("lamp".equals(device.getDeviceType())) {
                    lampCount++;
                }
            }
            overview.put("onlineDevices", onlineDevices);
            overview.put("lampCount", lampCount);
            
            return Result.success(overview);
        } catch (Exception e) {
            Log.e("获取校园概览失败: " + e.getMessage());
            return Result.error("获取校园概览失败: " + e.getMessage());
        }
    }
}
