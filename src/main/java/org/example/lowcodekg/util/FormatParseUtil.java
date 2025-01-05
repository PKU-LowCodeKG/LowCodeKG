package org.example.lowcodekg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类，格式转换
 */
public class FormatParseUtil {

    /**
     * 将字符串（int列表）转换成List<Integer>
     */
    public static List<Integer> parseIntList(String input) {
        List<Integer> result = new ArrayList<>();
        // 抽取输入字符串中被 [ ] 包围的部分
        String regex = "\\[.*?\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return result;
        }

        String extracted = matcher.group();
        if (extracted.startsWith("[") && extracted.endsWith("]")) {
            extracted = extracted.substring(1, extracted.length() - 1);
        }

        // 拆分字符串，按逗号分割
        String[] parts = extracted.split("\\s*,\\s*");

        // 将拆分后的部分转换成 Integer 并存入 List
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
