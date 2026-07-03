package com.wenhua.tcpservice.pojo;

import lombok.Data;

//查询参数类
@Data
public class QueryParameter {
    //当前页
    private Integer currentPage;
    //每页条数
    private Integer pageSize;
    //排序方式(升序降序)
    private String sort;
    //匹配字符串
    private String match;
    //用户名搜索
    private String username;
    //特殊:查询条件
    private Integer query = QUERY_ALL;
    //权限筛选
    private Integer permission;
    //偏移量(需要后期计算)
    private Integer offset;
    //查询在线
    public static final Integer QUERY_ONLINE = 1;
    //查询离线
    public static final Integer QUERY_OFFLINE = 2;
    //查询全部(默认就是)
    public static final Integer QUERY_ALL = 0;

    //计算偏移量
    public void calculateOffset(){
        this.offset = (this.currentPage - 1) * this.pageSize;
    }

    //常量
    public static final String DESC = "desc";
    public static final String ASC = "asc";

}
