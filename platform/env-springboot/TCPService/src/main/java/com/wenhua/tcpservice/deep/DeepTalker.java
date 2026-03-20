package com.wenhua.tcpservice.deep;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//deepseek传话器
public class DeepTalker implements Talker {
    //参数
    private Config config;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    //历史记录
    protected List<JSONObject> messages=new CopyOnWriteArrayList<>();
    //获取历史记录
    public synchronized List<JSONObject> getMessageHistory() {
        return new ArrayList<>(messages);
    }
    //测试方法:打印历史记录
    public void printHistory(){
        for (JSONObject message : messages) {
            System.out.println(message);
        }
    }
    //配置参数内部类
    private class Config {
        //配置参数
        //apikey
        private String apiKey;
        //URLbase
        private String urlBase;
        //模型名字
        private String modelName;
        //最大token数
        private int maxToken;
        //温度
        private float temperature;
        //空参构造-默认参数
        public Config() {
            //写一套默认的
            urlBase = "https://api.deepseek.com/v1/chat/completions"; // 假设这是默认API地址
            maxToken = 4000;  // 典型默认值
            temperature = 0.5f; // 默认随机性
            modelName = "deepseek-reasoner"; // 默认模型
            apiKey="sk-be71f2ddc3d94427a8c49f979eabd473";
        }
        //带参构造
        public Config(String apiKey, String urlBase, String modelName, int maxToken, float temperature) {
            this.apiKey = apiKey;
            this.urlBase = urlBase;
            this.modelName = modelName;
            this.maxToken = maxToken;
            this.temperature = temperature;
        }
    }

    //构造方法
    public DeepTalker() {
        this.config = new Config();
    }

    //发送请求获取结果方法
    //返回原始结果
    public String sendRequest(List<JSONObject> messages) throws IOException, InterruptedException {

        JSONObject body = new JSONObject()
                .put("model", config.modelName)
                .put("messages", new JSONArray(messages))
                .put("max_tokens", config.maxToken) // 添加
                .put("temperature", config.temperature) // 添加
                .put("enable_search", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.urlBase))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

// 修改后的状态码判断逻辑
        if (response.statusCode() != 200) {
            String errorBody = response.body();
            //LogUtil.d("[ERROR] API错误响应：\n" + errorBody);
            throw new RuntimeException("API请求失败，状态码: " + response.statusCode() + "\n响应内容：" + errorBody);
        }

        return response.body();

    }
    //创建消息
    public static JSONObject createMessage(String role, String content) {
        return new JSONObject()
                .put("role", role)
                .put("content", content);
    }

    @Override
    public String talk(String content) {
        //发送一次
        //先创建消息
        JSONObject message = createMessage("user", content);
        messages.add(message);
        //发送请求
        String res="";
        JSONObject aiMessage=null;
        try {
            res=sendRequest(messages);
            //成功则将res放入历史记录
            aiMessage = createMessage("assistant", getContent(res));

        }catch (Exception e){
            //出现异常,移除上次的用户上下文
            messages.remove(message);
            //e.printStackTrace();
            res="请求失败，请重试"+e.getMessage();
            return res;
        }

        messages.add(aiMessage);
        //返回结果
        return res;
    }

    /**
     * 从响应中提取助手的最终回复
     * @param jsonResponse 完整的API响应JSON字符串
     * @return 助手的最终回复内容
     */
    private static String getContent(String jsonResponse) {
        return new JSONObject(jsonResponse)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

    /**
     * 从响应中提取推理过程
     * @param jsonResponse 完整的API响应JSON字符串
     * @return 模型的推理过程内容
     */
    private static String getReasoningContent(String jsonResponse) {
        return new JSONObject(jsonResponse)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("reasoning_content");
    }


    @Override
    public void insertRecord(String userContent, String aiContent) {
        //插入-先创建消息
        JSONObject message = createMessage("user", userContent);
        JSONObject aiMessage = createMessage("assistant", aiContent);
        messages.add(message);
        messages.add(aiMessage);
    }

    @Override
    public void clearRecord() {
        this.messages=new ArrayList<>();
    }
}
