package com.wenhua.tcpservice.deep.tool;

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

//文件工具-实现文件写入
public class FileTool implements WorkerTool {

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

    @Override
    public String work(String content) {
        //检查是否包含文件标签
        if(!content.contains(FILE)){
            return "";
        }
        System.out.println("AI->"+content);
        //包含文件标签,进行处理
        StringBuffer sbRes=new StringBuffer();
        String[] todoFiles = XmlUtil.parseTagContents(FILE, content);
        for (String todoFile : todoFiles) {
            //进行操作-获取操作路径(只有一个操作路径)
            String filePath = XmlUtil.parseTagContent(PATH, todoFile);
            //进行操作
            if (todoFile.contains(xml(WRITE))) {
                //写
                //获取写入内容
                String writeContent = XmlUtil.parseTagContent(WRITE, todoFile);
                //矫正
                writeContent=formCorrection(writeContent);
                //写入文件
                boolean write = write(filePath, writeContent);
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
                boolean append = append(filePath, appendContent);
                if (append) {
                    sbRes.append("[追加写入成功]\n");
                }else{
                    sbRes.append("[追加写入失败]\n");
                }
            } else if (todoFile.contains(xml(READ))||todoFile.contains(xmlA(READ))) {
                //读
                String read = read(filePath);
                read=XmlUtil.generateTag("读取"+filePath,read);
                sbRes.append(read);
            }else if (todoFile.contains(xml(DELETE))||todoFile.contains(xmlA(DELETE))) {
                //删除
                boolean delete = delete(filePath);
                if (delete) {
                    sbRes.append("[删除成功]\n");
                }else{
                    sbRes.append("[删除失败]\n");
                }
            } else if (todoFile.contains(xml(LS))||todoFile.contains(xmlA(LS))) {
                //ls-展示目录下文件
                String ls = ls(filePath);
                sbRes.append(ls);
            } else if (todoFile.contains(xml(MKDIR))|| todoFile.contains(xmlA(MKDIR))) {
                //mkdir-创建目录
                boolean mkdir = mkdir(filePath);
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

    private String xml(String content){
        return "<"+content+">";
    }

    private String xmlA(String content){
        return "<"+content+"/>";
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

    // 读取文件内容
    private String read(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
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
            System.err.println("没有创建目录的权限: " + filePath);
            return false;
        }
    }
}
