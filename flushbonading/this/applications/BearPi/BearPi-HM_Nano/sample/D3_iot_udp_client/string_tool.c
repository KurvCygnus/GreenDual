# include "stdlib.h"
#include <string.h>
////////STRING.H///////////
//数据类型
typedef char* String;

String newString(char *str);
String concatString(char *str1, char *str2);

////////STRING.C////////////////////

// 构造方法
// 参数: 字符串
String newString(char *str) {
    if (str == NULL) {
        return NULL;
    }
    size_t len = strlen(str) + 1;  // +1 for null terminator
    String newStr = (char *)malloc(len * sizeof(char));
    if (newStr != NULL) {
        strcpy(newStr, str);
    }
    return newStr;
}

// 字符串拼接
// 参数: 两个字符串
// 返回: 拼接后的新字符串
String concatString(char *str1, char *str2) {
    if (str1 == NULL && str2 == NULL) {
        return NULL;
    }
    if (str1 == NULL) {
        return newString(str2);
    }
    if (str2 == NULL) {
        return newString(str1);
    }
    
    size_t len1 = strlen(str1);
    size_t len2 = strlen(str2);
    size_t totalLen = len1 + len2 + 1;  // +1 for null terminator
    
    String newStr = (char *)malloc(totalLen * sizeof(char));
    if (newStr != NULL) {
        strcpy(newStr, str1);
        strcat(newStr, str2);
    }
    return newStr;
}
