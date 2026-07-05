package com.wenhua.tcpservice.deep.tool;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

//文件工具-实现文件写入
@Slf4j
public final class FileTool implements WorkerTool {

    //文件工具
    //识别头
    private static final String FILE="file";
    //路径
    private static final String PATH="path";
    //操作
    //覆盖写入
    private static final String WRITE="write";
    //追加写入
    private static final String APPEND="append";
    //读取
    private static final String READ="read";
    //删除
    private static final String DELETE="delete";
    //ls-展示目录下文件
    private static final String LS="ls";
    //mkdir-创建目录
    private static final String MKDIR="mkdir";

    // 沙箱基目录（AI 文件操作被限制在此目录内）
    private static final String SANDBOX_DIR = System.getProperty("ai.sandbox.dir", "./ai-sandbox");

    // 禁止访问的系统关键路径
    private static final List<String> BLOCKED_PATH_PREFIXES = Arrays.asList(
        "/etc/", "/boot/", "/proc/", "/sys/", "/dev/",
        "/var/log/", "/var/db/", "/root/",
        "C:\\Windows\\", "C:\\Program Files\\", "C:\\ProgramData\\",
        "C:\\System32\\", "C:\\boot\\"
    );

    // 文件读取大小上限（1MB）
    private static final long MAX_READ_SIZE = 1024 * 1024;

    @Override
    public String work(String content) {
        //检查是否包含文件标签
        if(!content.contains(FILE)){
            return "";
        }
        log.debug("AI -> {}", content);
        //包含文件标签,进行处理
        StringBuilder sbRes=new StringBuilder();
        String[] todoFiles = XmlUtil.parseTagContents(FILE, content);
        for (String todoFile : todoFiles) {
            //进行操作-获取操作路径(只有一个操作路径)
            String filePath = XmlUtil.parseTagContent(PATH, todoFile);
            // 路径安全校验
            String validated = validatePath(filePath);
            if (validated.startsWith("[安全限制")) {
                sbRes.append(validated).append("\n");
                continue;
            }
            //进行操作
            if (todoFile.contains(xml(WRITE))) {
                //写
                //获取写入内容
                String writeContent = XmlUtil.parseTagContent(WRITE, todoFile);
                //矫正
                writeContent=formCorrection(writeContent);
                //写入文件
                boolean write = write(validated, writeContent);
                if (write) {
                    sbRes.append("[写入成功]\n");
                }else{
                    sbRes.append("[写入失败]\n");
                }
            } else if (todoFile.contains(xml(APPEND))) {
                //追加写
                //获取追加写入内容
                String appendContent = XmlUtil.parseTagContent(APPEND, todoFile);
                //矫正
                appendContent=formCorrection(appendContent);
                //追加写入文件
                boolean append = append(validated, appendContent);
                if (append) {
                    sbRes.append("[追加写入成功]\n");
                }else{
                    sbRes.append("[追加写入失败]\n");
                }
            } else if (todoFile.contains(xml(READ))||todoFile.contains(xmlA(READ))) {
                //读
                String read = read(validated);
                read=XmlUtil.generateTag("读取"+filePath,read);
                sbRes.append(read);
            }else if (todoFile.contains(xml(DELETE))||todoFile.contains(xmlA(DELETE))) {
                //删除
                boolean delete = delete(validated);
                if (delete) {
                    sbRes.append("[删除成功]\n");
                }else{
                    sbRes.append("[删除失败]\n");
                }
            } else if (todoFile.contains(xml(LS))||todoFile.contains(xmlA(LS))) {
                //ls-展示目录下文件
                String ls = ls(validated);
                sbRes.append(ls);
            } else if (todoFile.contains(xml(MKDIR))|| todoFile.contains(xmlA(MKDIR))) {
                //mkdir-创建目录
                boolean mkdir = mkdir(validated);
                if (mkdir) {
                    sbRes.append("[创建目录成功]\n");
                }else{
                    sbRes.append("[创建目录失败]\n");
                }
            }

        }

        return sbRes.toString();
    }

    //说明书
    @Override
    public String getInstruction() {
        String path = "personas/tool/文件工具.txt";
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

    /**
     * 校验路径是否在安全沙箱内，返回规范化后的路径字符串；
     * 若路径非法则返回错误描述（以 "[安全限制]" 开头）。
     */
    private static String validatePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return "[安全限制: 路径为空]";
        }
        try {
            Path resolved = Paths.get(filePath).toAbsolutePath().normalize();
            String canonical = resolved.toString();

            // 检查系统关键路径
            for (String blocked : BLOCKED_PATH_PREFIXES) {
                if (canonical.startsWith(blocked)) {
                    log.warn("AI 尝试访问系统关键路径: {}", canonical);
                    return "[安全限制: 禁止访问系统关键路径]";
                }
            }

            // 检查是否在沙箱目录内
            Path sandboxPath = Paths.get(SANDBOX_DIR).toAbsolutePath().normalize();
            if (!canonical.startsWith(sandboxPath.toString())) {
                log.warn("AI 尝试访问沙箱外路径: {}", canonical);
                return "[安全限制: 禁止访问沙箱目录外的路径]";
            }

            return canonical;
        } catch (Exception e) {
            log.error("路径校验异常: {}", e.getMessage());
            return "[安全限制: 路径校验失败]";
        }
    }

    private String xml(String content){
        return "<%s>".formatted(content);
    }

    private String xmlA(String content){
        return "<%s/>".formatted(content);
    }

    // 写入文件（覆盖模式）
    private boolean write(String filePath, String content) {
        if(filePath==null){
            return false;
        }
        Path path = Paths.get(filePath);

        //System.out.println("写入文件: "+filePath);
        //System.out.println("写入内容: "+content);

        try {
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    // 追加写入文件
    private boolean append(String filePath, String content) {
        if (filePath == null) {
            return false;
        }
        Path path = Paths.get(filePath);

        try {
            Files.write(
                    path,
                    content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    // 读取文件内容（上限 1MB）
    private String read(String filePath) {
        try {
            Path path = Paths.get(filePath);
            long size = Files.size(path);
            if (size > MAX_READ_SIZE) {
                return "[读取失败: 文件超过大小限制 (1MB)]";
            }
            return Files.readString(path);
        } catch (IOException e) {
            //e.printStackTrace();
            return "[读取失败]";
        }
    }

    // 删除文件
    private boolean delete(String filePath) {
        File file = new File(filePath);
        try {
            return file.delete();
        } catch (SecurityException e) {
            //e.printStackTrace();
            return false;
        }
    }
    //展示目录下文件,返回值类似:
    //路径 F://hello
    //文件:   1.txt
    //文件:   2.txt
    //文件:   hello.c
    //文件夹: 123
    //如果目录不存在,返回字符串"目录不存在"
    // 展示目录内容
    private String ls(String filePath) {
        File dir = new File(filePath);

        // 检查目录是否存在且有效
        if (!dir.exists() || !dir.isDirectory()) {
            return "目录不存在";
        }

        StringBuilder result = new StringBuilder();
        result.append("路径 ").append(dir.getAbsolutePath()).append("\n");

        // 获取并排序目录内容
        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files); // 按名称排序
            for (File file : files) {
                if (file.isDirectory()) {
                    result.append("文件夹: ").append(file.getName()).append("\n");
                } else {
                    result.append("文件:   ").append(file.getName()).append("\n");
                }
            }
        }
        return result.toString();
    }

    // 创建目录（支持多级目录）
    private boolean mkdir(String filePath) {
        File dir = new File(filePath);
        try {
            return dir.mkdirs();
        } catch (SecurityException e) {
            log.error("没有创建目录的权限: {}", filePath);
            return false;
        }
    }
}
