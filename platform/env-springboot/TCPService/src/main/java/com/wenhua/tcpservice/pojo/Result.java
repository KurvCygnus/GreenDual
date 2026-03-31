package com.wenhua.tcpservice.pojo;

import lombok.Getter;
import lombok.Setter;

/*@Data
@NoArgsConstructor
@AllArgsConstructor*/
@Setter
@Getter
public class Result {
    /**
     * -- GETTER --
     *  获取
     *
     *
     * -- SETTER --
     *  设置
     *
     @return code
      * @param code
     */
    private Integer code;//响应码，1 代表成功; 0 代表失败
    /**
     * -- GETTER --
     *  获取
     *
     *
     * -- SETTER --
     *  设置
     *
     @return msg
      * @param msg
     */
    private String msg;  //响应信息 描述字符串
    /**
     * -- GETTER --
     *  获取
     *
     *
     * -- SETTER --
     *  设置
     *
     @return data
      * @param data
     */
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
    
    /**
     * -- GETTER --
     *  获取
     *
     *
     * -- SETTER --
     *  设置
     *
     @return totalPages
      * @param totalPages
     */
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
    
    
    public String toString() {
        return "Result{code = " + code + ", msg = " + msg + ", data = " + data + "}";
    }
    
}
