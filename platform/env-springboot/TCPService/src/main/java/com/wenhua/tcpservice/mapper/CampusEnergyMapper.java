package com.wenhua.tcpservice.mapper;

import com.wenhua.tcpservice.pojo.campus.CampusArea;
import com.wenhua.tcpservice.pojo.campus.EnergyBill;
import com.wenhua.tcpservice.pojo.campus.TeachingCase;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CampusEnergyMapper {

    List<CampusArea> selectAreas(Map<String, Object> params);

    @Select("select count(*) from campus_area")
    int selectAreaCount();

    @Select("select * from campus_area where id = #{id}")
    CampusArea selectAreaById(Integer id);

    @Insert("insert into campus_area(area_name, area_type, area_size, lamp_count, device_count, manager_name, manager_phone, status) " +
            "values(#{areaName}, #{areaType}, #{areaSize}, #{lampCount}, #{deviceCount}, #{managerName}, #{managerPhone}, 1)")
    int insertArea(CampusArea area);

    @Update("update campus_area set " +
            "area_name = #{areaName}, " +
            "area_type = #{areaType}, " +
            "area_size = #{areaSize}, " +
            "lamp_count = #{lampCount}, " +
            "device_count = #{deviceCount}, " +
            "manager_name = #{managerName}, " +
            "manager_phone = #{managerPhone} " +
            "where id = #{id}")
    int updateArea(CampusArea area);

    @Delete("delete from campus_area where id = #{id}")
    int deleteArea(Integer id);

    List<EnergyBill> selectBills(Map<String, Object> params);

    @Select("select count(*) from energy_bill where area_id = #{areaId}")
    int selectBillCount(Integer areaId);

    @Select("select * from energy_bill where id = #{id}")
    EnergyBill selectBillById(Integer id);

    @Select("select * from energy_bill where area_id = #{areaId} and bill_month = #{billMonth}")
    EnergyBill selectBillByMonth(@Param("areaId") Integer areaId, @Param("billMonth") String billMonth);

    @Insert("insert into energy_bill(area_id, area_name, bill_month, total_consumption, total_cost, saved_consumption, saved_cost, saving_rate, " +
            "peak_consumption, off_peak_consumption, average_daily_consumption, waste_analysis, optimization_suggestions, status) " +
            "values(#{areaId}, #{areaName}, #{billMonth}, #{totalConsumption}, #{totalCost}, #{savedConsumption}, #{savedCost}, #{savingRate}, " +
            "#{peakConsumption}, #{offPeakConsumption}, #{averageDailyConsumption}, #{wasteAnalysis}, #{optimizationSuggestions}, 1)")
    int insertBill(EnergyBill bill);

    @Update("update energy_bill set status = #{status} where id = #{id}")
    int updateBillStatus(@Param("id") Integer id, @Param("status") Integer status);

    List<TeachingCase> selectTeachingCases(Map<String, Object> params);

    @Select("select count(*) from teaching_case where case_type = #{caseType}")
    int selectTeachingCaseCount(String caseType);

    @Select("select * from teaching_case where id = #{id}")
    TeachingCase selectTeachingCaseById(Integer id);

    @Select("select * from teaching_case where device_id = #{deviceId}")
    List<TeachingCase> selectTeachingCasesByDevice(String deviceId);

    @Insert("insert into teaching_case(case_name, case_type, description, device_id, device_name, data_source, " +
            "teaching_content, experiment_guide, related_course, use_count, status) " +
            "values(#{caseName}, #{caseType}, #{description}, #{deviceId}, #{deviceName}, #{dataSource}, " +
            "#{teachingContent}, #{experimentGuide}, #{relatedCourse}, 0, 1)")
    int insertTeachingCase(TeachingCase teachingCase);

    @Update("update teaching_case set use_count = use_count + 1 where id = #{id}")
    int incrementUseCount(Integer id);

    @Delete("delete from teaching_case where id = #{id}")
    int deleteTeachingCase(Integer id);
}
