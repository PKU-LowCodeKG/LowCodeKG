package org.example.lowcodekg.query.utils;

import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.core.SerializationHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class WekaTextClassifier {
    private static final String MODEL_FILE = "D:\\Master\\Data\\classification\\text_classification.model";
    private static final String JSON_DATASET = "D:\\Master\\Data\\classification\\dataset.json";
    private static Classifier classifier;
    private static Instances datasetFormat;

    public static void main(String[] args) throws Exception {
        // 1. 读取 JSON 数据集并转换为 Weka 的 ARFF 格式
//        Instances trainingData = loadJsonToWeka(JSON_DATASET);

        // 2. 训练分类器并保存模型
//        classifier = trainModel(trainingData);
//        SerializationHelper.write(MODEL_FILE, classifier);

        // 3. 加载模型并进行测试
        String testQuery = "对博客文章进行置顶";
        String predictedLabel = classifyText(testQuery);
        System.out.println("输入文本: " + testQuery);
        System.out.println("预测标签: " + predictedLabel);
    }

    /**
     * 加载 JSON 数据集，并转换为 Weka 训练数据
     */
    private static Instances loadJsonToWeka(String filePath) throws Exception {
        String jsonText = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONArray jsonArray = new JSONArray(jsonText);

        // 定义 Weka 数据结构
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("text", (ArrayList<String>) null)); // 文本属性
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("page");
        classValues.add("workflow");
        classValues.add("dataObject");
        attributes.add(new Attribute("class", classValues)); // 目标类别

        Instances dataset = new Instances("TextClassification", attributes, jsonArray.length());
        dataset.setClassIndex(1); // 类别是第二列

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String query = obj.getString("query");
            JSONArray labels = obj.getJSONArray("labels"); // 获取多标签（Weka 仅支持单标签）

            // 仅取第一个标签作为分类目标（Weka 不支持多标签）
            String mainLabel = labels.getString(0);

            double[] instanceValues = new double[2];
            instanceValues[0] = dataset.attribute(0).addStringValue(query); // 存文本
            instanceValues[1] = classValues.indexOf(mainLabel); // 存类别索引

            dataset.add(new DenseInstance(1.0, instanceValues));
        }

        // 保存为 ARFF 格式（可用于 Weka Explorer）
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataset);
        saver.setFile(new File("D:\\Master\\Data\\classification\\dataset.arff"));
        saver.writeBatch();

        return dataset;
    }

    /**
     * 训练 Weka 过滤器 + 分类器
     */
    private static FilteredClassifier trainModel(Instances trainingData) throws Exception {
        StringToWordVector filter = new StringToWordVector(); // 文本 -> 词向量
        filter.setInputFormat(trainingData);

        FilteredClassifier model = new FilteredClassifier();
        model.setFilter(filter);
        model.setClassifier(new RandomForest()); // 你可以换成 NaiveBayes、J48 等
        model.buildClassifier(trainingData);
        return model;
    }

    /**
     * 预测文本类别
     */
    public static String classifyText(String text) throws Exception {
        classifier = (FilteredClassifier) SerializationHelper.read(MODEL_FILE);
        if (classifier == null) {
            classifier = (FilteredClassifier) SerializationHelper.read(MODEL_FILE);
        }
        if (datasetFormat == null) {
            datasetFormat = loadJsonToWeka(JSON_DATASET);
        }

        DenseInstance instance = new DenseInstance(2);
        instance.setDataset(datasetFormat);
        instance.setValue(0, datasetFormat.attribute(0).addStringValue(text));

        double predictedIndex = classifier.classifyInstance(instance);
        return datasetFormat.classAttribute().value((int) predictedIndex);
    }
}
