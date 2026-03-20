package com.wenhua.tcpservice.deep.tool;


import java.util.ArrayList;
import java.util.List;

//xml工具
public class XmlUtil {
    //默认直接实现的方法
    /**
     * 解析给定字符串中的指定标签内容。
     *
     * @param tagName 标签名
     * @param input   包含标签的字符串
     * @return 标签内的内容，如果标签不存在则返回 null
     */
    public static String parseTagContent(String tagName, String input) {
        if (tagName == null || input == null) {
            return null;
        }

        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";

        int startIndex = input.indexOf(startTag);
        if (startIndex == -1) {
            return null;
        }

        startIndex += startTag.length();
        int endIndex = input.indexOf(endTag, startIndex);
        if (endIndex == -1) {
            return null;
        }

        String res=input.substring(startIndex, endIndex).trim();

        //LogUtil.w("\n[][][]"+res+"\n");
        return res;
    }

    /**
     * 解析给定字符串中的所有指定标签内容。
     *
     * @param tagName 标签名
     * @param input   包含多个标签的字符串
     * @return 包含所有标签内容的字符串数组，如果没有找到标签则返回空数组
     */
    public static String[] parseTagContents(String tagName, String input) {
        if (tagName == null || input == null) {
            return new String[0];
        }

        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        List<String> contents = new ArrayList<>();
        int index = 0;

        while ((index = input.indexOf(startTag, index)) != -1) {
            index += startTag.length();
            int endIndex = input.indexOf(endTag, index);
            if (endIndex == -1) {
                break;
            }
            contents.add(input.substring(index, endIndex).trim());
            index = endIndex + endTag.length();
        }

        return contents.toArray(new String[0]);
    }

    /**
     * 生成包含指定标签名的标签字符串。
     *
     * @param tagName 标签名
     * @param content 标签内的内容
     * @return 完整的标签字符串
     */
    public static String generateTag(String tagName, String content) {
        if (tagName == null || content == null) {
            return null;
        }

        return "<" + tagName + ">" + content + "</" + tagName + ">";
    }

}
