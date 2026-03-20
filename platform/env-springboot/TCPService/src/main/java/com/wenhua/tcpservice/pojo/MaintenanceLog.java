package com.wenhua.tcpservice.pojo;
import java.sql.Timestamp;
import lombok.Data;

//维修日志
@Data
public class MaintenanceLog {
    private Integer id;
    private Timestamp time;
    private String subject;
    private String content;
    private Integer userId;

    //写一个更新时间方法,使其时间变为当前时间
    // 更新时间方法，使其时间变为当前时间
    public void updateTimeToCurrent() {
        this.time = new Timestamp(System.currentTimeMillis());
    }
}
