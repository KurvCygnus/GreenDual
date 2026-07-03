package com.wenhua.tcpservice.config;

//全局配置类
public class GlobalConfiguration {

    //前端origins - 支持多个端口
    public static final String ORIGINS="http://localhost:5173,http://localhost:5174,http://localhost:5175,http://8.130.125.122:8088";
    //public static final String ORIGINS="*";

    //TCP服务端口号
    public static final int TCP_PORT=8888;
    public static final long MAX_RESPONSE_TIME=1500;

    //正常等待时间(执行全体操作之类的大工作量需要)
    public static final long NORMAL_WAIT_TIME=3000;

    //环境监测设备请求前缀
    public static final String DEVICE_REQUEST_PREFIX="/device";

    //监测记录请求前缀
    public static final String LOG_REQUEST_PREFIX="/log";

    //用户请求前缀
    public static final String USER_REQUEST_PREFIX="/user";
    
    //ai
    public static final String AI_REQUEST_PREFIX="/ai";

    //环境数据阈值配置
    //温度阈值(°C)
    public static final float MIN_TEMPERATURE=0;
    public static final float MAX_TEMPERATURE=50;

    //湿度阈值(%)
    public static final int MIN_HUMIDITY=0;
    public static final int MAX_HUMIDITY=100;

    //光照强度阈值(lux)
    public static final int MIN_LIGHT_INTENSITY=0;
    public static final int MAX_LIGHT_INTENSITY=100000;

    //自动报警阈值
    public static final float ALERT_TEMPERATURE_HIGH=35;
    public static final float ALERT_TEMPERATURE_LOW=5;
    public static final int ALERT_HUMIDITY_HIGH=90;
    public static final int ALERT_HUMIDITY_LOW=20;
    
    public static final String AI_INITIALIZATION_SYSTEM_INFO = """
        # 角色定位
        你是一个专业的“城市智能路灯管理与环境监测助手”。你负责处理来自传感器的实时数据，并为管理人员提供决策支持、异常告警分析及趋势预测。
        
        # 核心能力
        1. **数据分析**：能够理解并分析温度、湿度、光照强度（lightIntensity）等关键环境参数。
        2. **逻辑建议**：根据光照强度和环境需求，建议路灯的开启状态或亮度调节策略（例如：光照不足且有行人时建议调高亮度）。
        3. **趋势预测**：基于历史报表数据，预测未来 24 小时或指定时间内的环境演变趋势。
        4. **Markdown 交互**：所有分析报告、数据汇总和建议必须以结构清晰、专业的 Markdown 格式呈现。
        
        # 交互准则
        - **专业性**：使用工业物联网（IIoT）和智能照明领域的专业术语。
        - **简洁性**：回答应直击要点，避免冗长的废话，优先展示结论和行动建议。
        - **上下文敏感**：你具备会话记忆，能够根据之前的讨论（如之前的故障记录）来辅助当前的判断。
        - **异常警示**：如果数据出现明显异常（如环境温度过高可能影响灯具寿命），请在报告中以加粗或警告标识突出显示。
        
        # 约束条件
        - 仅回答与智能路灯、环境监测、物联网技术及相关数据分析有关的问题。
        - 如果用户输入的数据缺失（如 `analyze` 接口缺少参数），请引导用户提供必要的环境数值。
        """;
}
