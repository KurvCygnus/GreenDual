package com.wenhua.tcpservice.deep;

import com.wenhua.tcpservice.deep.tool.WorkerTool;
import org.json.JSONObject;

import java.util.List;

//工作者
public abstract class Worker {
    //是否需要日志1
    protected final boolean needLog = true;
    private void log(String  l){
        if (needLog){
            System.out.println("log->"+l);
        }
    }
    //通话器
    public ThreadSafeStringStorage storage=new ThreadSafeStringStorage();
    //工具箱
    protected final List<WorkerTool> tools;

    //人设
    protected final String workerSetting;

    //传话器
    protected final Talker talker;
    //获取历史记录
    public abstract List<JSONObject> getWorkerTalk();
    //全参构造
    public Worker(List<WorkerTool> tools, String workerSetting, Talker talker) {
        this.tools = tools;
        this.workerSetting = workerSetting;
        this.talker = talker;
    }

    //工作方法
    public String work(String task){

        //根据人设构造传话
        String content = workerSetting/* + task*/;
        //工具

        for (WorkerTool tool : tools) {
            content+=tool.getInstruction();
        }
        content+="[上级要求]:"+task;

        while (true){
            //传话
            onSend(content);
            log("发送"+content);
            String result = talker.talk(content);
            log("收到"+result);
            onResult(result);
            result=parse(result);
            //返回给工人的内容
            StringBuffer sb = new StringBuffer();
            //对于工作者的回复,调用工具去做事
            for (WorkerTool tool : tools){
                String workRes = tool.work(result);
                sb.append(workRes);
            }

            //弄完后,将content变成
            content = sb.toString();
            log("执行结果"+content);

            //如果发现可以结束
            if (isEndWork(result)){
                log("结束了工作");
                return content;
            }
        }

        /*return "";*/
    }

    //结束工作判断方法
    protected abstract boolean isEndWork(String content);

    //发送时字符串
    protected abstract void onSend(String content);
    //结果字符串
    protected abstract void onResult(String content);

    //解析方法
    public abstract String parse(String content);

}
