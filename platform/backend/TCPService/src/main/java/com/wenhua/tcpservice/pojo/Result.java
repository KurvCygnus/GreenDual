package com.wenhua.tcpservice.pojo;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter @ToString
public final class Result
{
    private final int code;
    private final String msg;
    private final Object data;
    private final Integer totalPages;
    
    private static final int SUCCESS_CODE = 1;
    private static final int ERROR_CODE = 0;
    
    public static final String BUSY = "设备繁忙";
    public static final String NOT_EXIST = "设备不存在";
    public static final String EXIST = "设备已存在";
    public static final String TIMEOUT = "响应超时";
    
    private Result(int code, String msg, Object data, Integer totalPages)
    {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.totalPages = totalPages;
    }
    
    public static @NonNull Result success() { return new Result(SUCCESS_CODE, "success", null, null); }
    
    public static @NonNull Result success(Object data) { return new Result(SUCCESS_CODE, "success", data, null); }
    
    public static @NonNull Result success(Object data, Integer totalPages) { return new Result(SUCCESS_CODE, "success", data, totalPages); }
    
    public static @NonNull Result error(String msg) { return new Result(ERROR_CODE, msg, null, null); }
    
    /**
     * 从布尔值构造（true=成功，false=失败）。
     */
    public static @NonNull Result of(boolean success) { return success ? success() : error("操作失败"); }
    
    /**
     * 设置分页数并返回新对象（不可变链式调用）。
     */
    public @NonNull Result totalPages(int totalPages) { return new Result(this.code, this.msg, this.data, totalPages); }
}