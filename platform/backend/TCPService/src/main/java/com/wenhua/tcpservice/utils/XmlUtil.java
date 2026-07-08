package com.wenhua.tcpservice.utils;


import java.util.ArrayList;
import java.util.List;

//xml工具
public final class XmlUtil
{
    private static final String[] EMPTY_STR = new String[0];
    
    private XmlUtil() { throw new IllegalAccessError("Class \"XmlUtil\" is not meant to be instantized!"); }
    
    //默认直接实现的方法
    
    /**
     * 解析给定字符串中的指定标签内容。
     *
     * @param tagName 标签名
     * @param input   包含标签的字符串
     * @return 标签内的内容，如果标签不存在则返回 null
     */
    public static String parseTagContent(String tagName, String input)
    {
        if(tagName == null || input == null)
            return null;
        
        final var startTag = "<%s>".formatted(tagName);
        final var endTag = "</%s>".formatted(tagName);
        
        var startIndex = input.indexOf(startTag);
        if(startIndex == -1)
            return null;
        
        startIndex += startTag.length();
        final var endIndex = input.indexOf(endTag, startIndex);
        if(endIndex == -1)
            return null;
        
        return input.substring(startIndex, endIndex).trim();
    }
    
    /**
     * 解析给定字符串中的所有指定标签内容。
     *
     * @param tagName 标签名
     * @param input   包含多个标签的字符串
     * @return 包含所有标签内容的字符串数组，如果没有找到标签则返回空数组
     */
    public static String[] parseTagContents(String tagName, String input)
    {
        if(tagName == null || input == null)
            return EMPTY_STR;
        
        final var startTag = "<%s>".formatted(tagName);
        final var endTag = "</%s>".formatted(tagName);
        final List<String> contents = new ArrayList<>();
        var index = 0;
        
        while((index = input.indexOf(startTag, index)) != -1)
        {
            index += startTag.length();
            final var endIndex = input.indexOf(endTag, index);
            if(endIndex == -1)
                break;
            contents.add(input.substring(index, endIndex).trim());
            index = endIndex + endTag.length();
        }
        
        return contents.toArray(EMPTY_STR);
    }
    
    /**
     * 生成包含指定标签名的标签字符串。
     *
     * @param tagName 标签名
     * @param content 标签内的内容
     * @return 完整的标签字符串
     */
    public static String generateTag(String tagName, String content)
    {
        if(tagName == null || content == null)
            return null;
        
        return StringFormatter.format("<{}>{}</{}>", tagName, content, tagName);
    }
}
