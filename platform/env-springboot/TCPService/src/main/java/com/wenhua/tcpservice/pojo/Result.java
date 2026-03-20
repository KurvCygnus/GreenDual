package com.wenhua.tcpservice.pojo;

/*@Data
@NoArgsConstructor
@AllArgsConstructor*/
public class Result {
    private Integer code;//响应码，1 代表成功; 0 代表失败
    private String msg;  //响应信息 描述字符串
    private Object data; //返回的数据

    //响应码
    //成功
    public static final Integer SUCCESS = 1;
    //失败
    public static final Integer ERROR = 0;

    //设备繁忙
    public static final String BUSY = "设备繁忙";
    //设备不存在
    public static final String NOT_EXIST = "设备不存在";
    //设备已存在
    public static final String EXIST = "设备已存在";
    //响应超时
    public static final String TIMEOUT = "响应超时";

    //特殊
    //页面数量
    private Integer totalPages;
    public Result() {
    }

    public Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(Integer code, String msg, Object data, Integer totalPages) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.totalPages = totalPages;
    }


    //增删改 成功响应
    public static Result success(){
        return new Result(1,"success",null);
    }
    //查询 成功响应
    public static Result success(Object data){
        return new Result(1,"success",data);
    }

    //查询 成功响应
    public static Result success(Object data,Integer totalPages){
        return new Result(1,"success",data,totalPages);
    }


    //失败响应
    public static Result error(String msg){
        return new Result(0,msg,null);
    }

    //构造方法,通过bool构造
    public Result (boolean bool){
        if(bool){
            this.code = SUCCESS;
        }else
            this.code = ERROR;
    }


    /**
     * 获取
     * @return code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置
     * @param code
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 获取
     * @return msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置
     * @param msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取
     * @return data
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置
     * @param data
     */
    public void setData(Object data) {
        this.data = data;
    }

    public String toString() {
        return "Result{code = " + code + ", msg = " + msg + ", data = " + data + "}";
    }

    /**
     * 获取
     * @return totalPages
     */
    public Integer getTotalPages() {
        return totalPages;
    }

    /**
     * 设置
     * @param totalPages
     */
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
