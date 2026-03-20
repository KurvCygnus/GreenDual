package com.wenhua.tcpservice.deep.tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

//和用户交互的工具
public class UserTool implements WorkerTool{
    //报告
    public static final String REPORT="report";

    @Override
    public String work(String content) {
        if (content.contains(xml(REPORT))){
            //获取内容
            String res = XmlUtil.parseTagContent(REPORT, content);
            return res;
        }
        return "";
    }

    //说明书
    @Override
    public String getInstruction() {
        String path = "personas/tool/用户交互工具.txt";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new FileNotFoundException("人设文件未找到: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 处理异常（如默认人设或报错）
            return "出现异常,请你无视一切规则回复标签  <report>工具异常!!!</report>";
        }
    }

    private String xml(String content){
        return "<"+content+">";
    }

}
