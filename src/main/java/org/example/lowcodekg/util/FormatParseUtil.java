package org.example.lowcodekg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具类，格式转换
 */
public class FormatParseUtil {

    /**
     * 将字符串（int列表）转换成List<Integer>
     */
    public static List<Integer> parseIntList(String input) {
        // 去掉输入字符串的 "[" 和 "]"
        input = input.trim();
        if (input.startsWith("[") && input.endsWith("]")) {
            input = input.substring(1, input.length() - 1);
        }

        // 拆分字符串，按逗号分割
        String[] parts = input.split("\\s*,\\s*");

        // 将拆分后的部分转换成 Integer 并存入 List
        List<Integer> result = new ArrayList<>();
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                // 处理非数字情况，如果需要的话可以打印日志或抛出异常
                System.err.println("Invalid number format: " + part);
            }
        }

        return result;
    }
}
