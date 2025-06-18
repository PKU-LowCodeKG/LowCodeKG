package org.example.lowcodekg.query.service.evaluation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * 数据处理类
 */
public class DataProcess {


    /**
     * 从JSON文件读取数据
     * @param filePath JSON文件路径
     * @return JSON对象
     * @throws IOException 如果文件读取失败
     */
    public static JSONObject loadDataFromJson(String filePath) throws IOException {
        File file = new File(filePath);
        try (FileReader reader = new FileReader(file)) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                content.append(buffer, 0, length);
            }
            return JSON.parseObject(content.toString());
        }
    }

    /**
     * 获取指定查询的推荐结果
     * @param jsonObject JSON对象
     * @param query 查询字符串
     * @return 推荐结果列表
     */
    public List<String> getResultsForQuery(JSONObject jsonObject, String query) {
        if (jsonObject == null) {
            return Collections.emptyList();
        }

        JSONArray dataset = jsonObject.getJSONArray("dataset");
        if (dataset != null) {
            for (int i = 0; i < dataset.size(); i++) {
                JSONObject entry = dataset.getJSONObject(i);
                if (query.equals(entry.getString("query"))) {
                    return entry.getJSONArray("result").toJavaList(String.class);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 获取查询结果映射
     * @return 查询到结果列表的映射
     */
    public static Map<String, List<String>> getQueryResultMap(String path) {
        JSONObject jsonObject = null;
        try {
            jsonObject = loadDataFromJson(path);
            if (jsonObject == null) {
                return Collections.emptyMap();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, List<String>> queryResultMap = new HashMap<>();
        JSONArray dataset = jsonObject.getJSONArray("dataset");
        if (dataset != null) {
            for (int i = 0; i < dataset.size(); i++) {
                JSONObject entry = dataset.getJSONObject(i);
                String query = entry.getString("query");
                List<String> results = entry.getJSONArray("result").toJavaList(String.class);
                queryResultMap.put(query, results);
            }
        }
        return queryResultMap;
    }
}
