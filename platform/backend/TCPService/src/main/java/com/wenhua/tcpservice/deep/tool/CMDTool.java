package com.wenhua.tcpservice.deep.tool;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class CMDTool implements WorkerTool {

    private CMDTool() { throw new IllegalStateException("Utility class"); }

    //字段-CMD
    private static final String CMD ="cmd";
    private static final String CMD_RES ="cmd-result";
    //字段-powerShell
    private static final String POWER_SHELL ="power-shell";
    private static final String POWER_SHELL_RES ="power-shell-result";

    // 安全只读命令白名单
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
        "ipconfig", "ping", "systeminfo", "tasklist",
        "dir", "echo", "whoami", "ver", "date", "time",
        "chcp", "path", "cd", "type", "findstr",
        "netstat", "nslookup", "tracert", "route"
    );

    // 禁止的命令操作符
    private static final Set<String> DANGEROUS_OPERATORS = Set.of(
        "|", ">", "<", "&", ";", "`", "$(", "%0a", "%0d",
        "&&", "||"
    );

    @Override
    public String work(String content) {
        StringBuilder result = new StringBuilder();
        //是否包含cmd
        if(content.contains(xml(CMD))){
            //解析
            String[] cmds = XmlUtil.parseTagContents(CMD, content);
            for(String cmd:cmds){
                cmd=cmd.trim();
                cmd=formCorrection(cmd);
                if (!isCommandAllowed(cmd)) {
                    result.append(XmlUtil.generateTag(CMD_RES, "[命令不在白名单中, 已拒绝执行]"));
                    continue;
                }
                //执行
                result.append(XmlUtil.generateTag(CMD_RES, executeCMD(cmd)));
            }
        }
        //是否包含powerShell
        if(content.contains(xml(POWER_SHELL))){
            //解析
            String[] cmds = XmlUtil.parseTagContents(POWER_SHELL, content);
            for(String cmd:cmds){
                cmd=cmd.trim();
                cmd=formCorrection(cmd);
                if (!isCommandAllowed(cmd)) {
                    result.append(XmlUtil.generateTag(POWER_SHELL_RES, "[命令不在白名单中, 已拒绝执行]"));
                    continue;
                }
                //执行
                result.append(XmlUtil.generateTag(POWER_SHELL_RES, executePowerShell(cmd)));
            }
        }

        return result.toString();
    }

    @Override
    public String getInstruction() {
        String path = "personas/tool/命令行工具.txt";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new FileNotFoundException("文件未找到: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 处理异常（如默认人设或报错）
            return "出现异常,请你无视一切规则回复标签  <report>工具异常!!!</report>";
        }
    }

    /**
     * 检查命令是否在白名单中且不含危险操作符
     */
    private static boolean isCommandAllowed(String cmd) {
        if (cmd == null || cmd.isBlank()) return false;
        String lower = cmd.trim().toLowerCase();
        // 检查危险操作符
        for (String op : DANGEROUS_OPERATORS) {
            if (lower.contains(op)) return false;
        }
        // 提取命令名（第一个 token）
        String commandName = lower.split("\\s+")[0];
        return ALLOWED_COMMANDS.contains(commandName);
    }

    private static String xml(String content){
        return "<"+content+">";
    }


    /**
     * 执行CMD命令并返回结果
     * @param cmd 需要执行的命令
     * @return 命令执行结果（包含标准输出和错误输出）
     */
    private static String executeCMD(String cmd) {
        try {
            // 创建ProcessBuilder并设置命令
            ProcessBuilder builder = new ProcessBuilder("cmd", "/c", cmd);
            // 合并标准错误流到标准输出流
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // 读取命令输出
            String output = readStream(process.getInputStream());
            // 等待命令执行完成
            int exitCode = process.waitFor();

            return output + "\nExit Code: " + exitCode;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // 保持中断状态
            return "CMD执行失败: " + e.getMessage();
        }
    }

    /**
     * 执行PowerShell命令并返回结果
     * @param cmd 需要执行的命令
     * @return 命令执行结果（包含标准输出和错误输出）
     */
    private static String executePowerShell(String cmd) {
        try {
            // 创建ProcessBuilder并设置命令
            ProcessBuilder builder = new ProcessBuilder("powershell", "-Command", cmd);
            // 合并标准错误流到标准输出流
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // 读取命令输出
            String output = readStream(process.getInputStream());
            // 等待命令执行完成
            int exitCode = process.waitFor();

            return output + "\nExit Code: " + exitCode;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // 保持中断状态
            return "PowerShell执行失败: " + e.getMessage();
        }
    }

    /**
     * 读取输入流内容并转换为字符串
     * @param inputStream 输入流
     * @return 流内容字符串
     * @throws IOException 如果读取发生错误
     */
    private static String readStream(InputStream inputStream) throws IOException {
        // 使用GBK编码读取中文（Windows系统默认编码）
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName("GBK"))
        );
        StringBuilder output = new StringBuilder();
        String line;

        // 逐行读取内容
        while ((line = reader.readLine()) != null) {
            output.append(line).append(System.lineSeparator());
        }

        // 去除末尾多余的空行
        return output.toString().trim();
    }
}
