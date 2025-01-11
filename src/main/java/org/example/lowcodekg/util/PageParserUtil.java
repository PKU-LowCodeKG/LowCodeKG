package org.example.lowcodekg.util;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageParserUtil {
    public static String getScriptMethod(String content) {
        String[] lines = content.split("\n");
        List<String> lineList = new ArrayList<>(Arrays.asList(lines));
        StringBuilder dataBlock = new StringBuilder();
        for(int i = 0;i < lineList.size();i++) {
            if(lineList.get(i).contains("methods: {")) {
                int j = i + 1;
                String intent = getScriptIndent(lineList.get(i));
                while(j < lineList.size()) {
                    dataBlock.append(lineList.get(j) + "\n");
                    j++;
                    if(j >= lineList.size()
                            || lineList.get(j).equals(intent + "},")
                            || lineList.get(j).equals(intent + "}"))
                        break;
                }
                break;
            }
        }
        return dataBlock.toString();
    }

    public static String getScriptIndent(String line) {
        StringBuilder indentation = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indentation.append(" ");
            } else if (c == '\t') {
                indentation.append('\t');
            } else {
                break;
            }
        }
        return indentation.toString();
    }

    public static String getScriptData(String content) {
        String[] lines = content.split("\n");
        List<String> lineList = new ArrayList<>(Arrays.asList(lines));
        StringBuilder dataBlock = new StringBuilder();
        for(int i = 0;i < lineList.size();i++) {
            if(lineList.get(i).contains("data")) {
                if((lineList.get(i).contains("data()") || lineList.get(i).contains("function"))
                        && i+1 < lineList.size() && lineList.get(i+1).contains("return {")) {
                    int j = i + 2;
                    String intent = getScriptIndent(lineList.get(i));
                    while (j < lineList.size()) {
                        String tmp = lineList.get(j);
                        if(tmp.contains("//")) { // remove comment
                            tmp = tmp.substring(0, tmp.indexOf("//"));
                        }
                        dataBlock.append(tmp);
                        j++;
                        if (j == lineList.size() || lineList.get(j).equals(intent + "},")
                                || lineList.get(j).equals(intent + "}"))
                            break;
                    }
                    break;
                }
            }
        }
        if(dataBlock.length() == 0) {
            return null;
        }
        dataBlock.insert(0, " { ");
        return dataBlock.toString();
    }

    public static String getScriptContent(String fileContent) {
        StringBuilder scriptContent = new StringBuilder();
        List<String> lines = Arrays.asList(fileContent.split("\n"));
        for(int i = 0;i < lines.size();i++) {
            if(lines.get(i).contains("<script")) {
                int j = i + 1;
                while(j < lines.size() && !lines.get(j).contains("</script>")) {
                    scriptContent.append(lines.get(j)).append("\n");
                    j++;
                }
                break;
            }
        }
        return scriptContent.toString();
    }

    public static String getTemplateContent(String fileContent) {
        Pattern pattern = Pattern.compile("<template>(.*?)</template>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fileContent);
        if (matcher.find()) {
            return matcher.group(0).trim(); // 返回去除前后空白的模板内容
        } else {
            return null;
        }
    }

    public static JSONObject parseImportsComponent(String content) {
        try {
            JSONObject importsList = new JSONObject();
            String importPattern = "import\\s*\\{?\\s*([\\w,\\s]+)\\s*\\}?\\s*from\\s*['\"]([^'\"]+)['\"]";
            Pattern pattern = Pattern.compile(importPattern);
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String names = matcher.group(1).trim();
                String path = matcher.group(2).trim();

                String[] nameArray = names.split("\\s*,\\s*");
                for (String name : nameArray) {
                    importsList.put(name, path);
                }
            }
            return importsList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
