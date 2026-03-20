package com.wenhua.tcpservice.utils;

import org.json.JSONObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class JsonUtils {

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 要转换的对象
     * @return JSON字符串表示
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return "null";
        }

        JSONObject jsonObject = new JSONObject();
        Class<?> clazz = obj.getClass();

        // 从当前类的Class对象开始，向上遍历所有父类
        while (clazz != null && clazz != Object.class) {
            // 获取所有声明的字段（包括私有字段）
            Field[] fields = clazz.getDeclaredFields();

            try {
                for (Field field : fields) {
                    // 检查字段是否是静态的，如果是则跳过
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    // 设置字段为可访问，以便访问私有字段
                    field.setAccessible(true);

                    // 获取字段的值
                    Object value = field.get(obj);

                    // 如果值不为null，或者字段是基本数据类型（其包装类也不会为null，但这里为了安全起见还是检查）
                    // 注意：对于复杂对象，这里不会递归转换，可能需要额外的处理
                    if (value != null || field.getType().isPrimitive()) {
                        // 对于基本数据类型，直接放入；对于对象类型，如果非null也放入（但可能需要递归转换）
                        jsonObject.put(field.getName(), value);
                    }
                    // 注意：这里简化了处理，没有处理复杂对象（如集合、数组等）的递归转换
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                // 可以选择抛出异常、记录日志或返回null等，取决于你的错误处理策略
                // 这里为了简单起见，只是打印了堆栈跟踪
            }

            // 继续向上遍历父类
            clazz = clazz.getSuperclass();
        }

        // 返回JSON字符串表示
        return jsonObject.toString();
    }

    // 注意：这个工具类没有处理循环引用、复杂对象（如集合、数组、Map等）的递归转换等问题
    // 在实际应用中，可能需要使用更专业的JSON序列化库（如Jackson、Gson等）来处理这些情况
}