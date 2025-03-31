package org.example.lowcodekg.query.service.classification;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WekaTextProcessor {
    private Map<String, Classifier> labelClassifiers;
    private StringToWordVector filter;
    private Instances dataStructure;
    private List<String> labels;
    private double threshold = 0.5;
    private List<TextClassificationData> trainingData; // 新增成员变量

    // 在类的成员变量部分添加模型保存路径
    private static final String MODEL_SAVE_PATH = "D:\\Master\\Data\\classification\\";
    private static final String FILTER_FILE = MODEL_SAVE_PATH + "filter.model";
    private static final String LABELS_FILE = MODEL_SAVE_PATH + "labels.json";

    public WekaTextProcessor() {
        labelClassifiers = new HashMap<>();
        filter = new StringToWordVector();
        // 优化文本预处理参数
        filter.setWordsToKeep(3000);  // 增加特征词数量
        filter.setOutputWordCounts(true);
        filter.setTFTransform(true);
        filter.setIDFTransform(true);
        filter.setLowerCaseTokens(true);
        filter.setDoNotOperateOnPerClassBasis(true);
        filter.setMinTermFreq(1);  // 降低词频阈值，保留更多特征
        filter.setStemmer(new weka.core.stemmers.IteratedLovinsStemmer());  // 添加词干提取
        filter.setStopwordsHandler(new weka.core.stopwords.WordsFromFile());  // 添加停用词
        filter.setTokenizer(new weka.core.tokenizers.NGramTokenizer());  // 使用N-gram特征
        threshold = 0.4;  // 调整预测阈值
    }

    public void buildClassifier(List<TextClassificationData> trainingData) throws Exception {
        this.trainingData = trainingData; // 保存训练数据的引用
        // Prepare data structure and labels
        prepareDataStructure(trainingData);
        
        // Create training instances
        Instances trainInstances = createInstances(trainingData);
        
        // Apply filter
        filter.setInputFormat(trainInstances);
        Instances filteredData = Filter.useFilter(trainInstances, filter);
        
        // Train a classifier for each label
        for (String label : labels) {
            RandomForest classifier = new RandomForest();
            // Set RandomForest options
            classifier.setOptions(weka.core.Utils.splitOptions("-I 100 -K 0 -depth 0"));
            Instances labelData = createBinaryLabelData(filteredData, label);
            classifier.buildClassifier(labelData);
            labelClassifiers.put(label, classifier);
        }
    }

    public List<String> predict(String text) throws Exception {
        // Create test instance
        Instance instance = createInstance(text);
        
        // Apply filter
        Instances testInstances = new Instances(dataStructure, 0);
        testInstances.add(instance);
        Instances filteredTest = Filter.useFilter(testInstances, filter);
        
        // Predict for each label
        List<String> predictedLabels = new ArrayList<>();
        System.out.println("预测置信度:");
        for (String label : labels) {
            Instances labelData = createBinaryLabelData(filteredTest, label);
            double[] distribution = labelClassifiers.get(label).distributionForInstance(labelData.get(0));
            double confidence = distribution[1];  // 获取正类的概率
            if (confidence >= threshold) {
                predictedLabels.add(label);
            }
            System.out.printf("  %s: %.2f%%\n", label, confidence * 100);
        }
        
        return predictedLabels;
    }

    private void prepareDataStructure(List<TextClassificationData> data) {
        // Collect unique labels
        Set<String> uniqueLabels = new HashSet<>();
        for (TextClassificationData item : data) {
            uniqueLabels.addAll(item.getLabels());
        }
        labels = new ArrayList<>(uniqueLabels);
        Collections.sort(labels);

        // Create attributes
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("text", (ArrayList<String>) null));
        attributes.add(new Attribute("class", Arrays.asList("0", "1")));

        // Create data structure
        dataStructure = new Instances("TextClassification", attributes, 0);
        dataStructure.setClassIndex(1);
    }

    private Instance createInstance(String text) {
        double[] values = new double[2];
        values[0] = dataStructure.attribute(0).addStringValue(text);
        values[1] = 0;  // Default class value
        
        Instance instance = new DenseInstance(1.0, values);
        instance.setDataset(dataStructure);
        return instance;
    }

    private Instances createInstances(List<TextClassificationData> data) {
        Instances instances = new Instances(dataStructure);
        
        for (TextClassificationData item : data) {
            double[] values = new double[2];
            values[0] = instances.attribute(0).addStringValue(item.getQuery());
            values[1] = 0;  // Will be set per label in createBinaryLabelData
            instances.add(new DenseInstance(1.0, values));
        }
        
        return instances;
    }

    private Instances createBinaryLabelData(Instances data, String targetLabel) {
        // 创建新的数据结构，保持与输入数据相同的属性
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < data.numAttributes(); i++) {
            attributes.add((Attribute) data.attribute(i).copy());
        }
        
        // 创建新的Instances对象
        Instances labelData = new Instances("BinaryLabel", attributes, data.numInstances());
        labelData.setClassIndex(data.classIndex());
        
        // 复制实例并设置标签值
        for (int i = 0; i < data.numInstances(); i++) {
            DenseInstance newInst = new DenseInstance(data.instance(i));
            newInst.setDataset(labelData);
            labelData.add(newInst);
            
            // 如果是训练阶段，使用训练数据的标签
            if (trainingData != null) {
                TextClassificationData originalItem = trainingData.get(i);
                boolean hasLabel = originalItem.getLabels().contains(targetLabel);
                labelData.instance(i).setClassValue(hasLabel ? 1.0 : 0.0);
            } else {
                // 预测阶段，设置默认值
                labelData.instance(i).setClassValue(0.0);
            }
        }
        
        return labelData;
    }

    // 添加保存模型的方法
    public void saveModel() throws Exception {
        // 创建保存目录
        new File(MODEL_SAVE_PATH).mkdirs();
        
        // 保存过滤器
        weka.core.SerializationHelper.write(FILTER_FILE, filter);
        
        // 保存每个标签的分类器
        for (Map.Entry<String, Classifier> entry : labelClassifiers.entrySet()) {
            String classifierFile = MODEL_SAVE_PATH + entry.getKey() + ".model";
            weka.core.SerializationHelper.write(classifierFile, entry.getValue());
        }
        
        // 保存标签列表
        Files.write(Paths.get(LABELS_FILE), new Gson().toJson(labels).getBytes());
    }

    // 添加加载模型的方法
    public void loadModel() throws Exception {
        // 加载过滤器
        filter = (StringToWordVector) weka.core.SerializationHelper.read(FILTER_FILE);
        
        // 加载标签列表
        labels = new Gson().fromJson(
            new String(Files.readAllBytes(Paths.get(LABELS_FILE))),
            new TypeToken<List<String>>(){}.getType()
        );
        
        // 初始化数据结构
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("text", (ArrayList<String>) null));
        attributes.add(new Attribute("class", Arrays.asList("0", "1")));
        dataStructure = new Instances("TextClassification", attributes, 0);
        dataStructure.setClassIndex(1);
        
        // 加载每个标签的分类器
        labelClassifiers = new HashMap<>();
        for (String label : labels) {
            String classifierFile = MODEL_SAVE_PATH + label + ".model";
            Classifier classifier = (Classifier) weka.core.SerializationHelper.read(classifierFile);
            labelClassifiers.put(label, classifier);
        }
    }

    public static void main(String[] args) {
        try {
            WekaTextProcessor processor = new WekaTextProcessor();
            File modelFile = new File(MODEL_SAVE_PATH + "workflow.model");
            
            if (!modelFile.exists()) {
                // 如果模型不存在，进行训练
                System.out.println("模型不存在，开始训练...");
                
                // 从JSON文件加载训练数据
                String dataPath = "D:\\Master\\Data\\classification\\dataset.json";
                List<TextClassificationData> trainingData = new Gson().fromJson(
                    new String(Files.readAllBytes(Paths.get(dataPath))),
                    new TypeToken<List<TextClassificationData>>(){}.getType()
                );
                
                System.out.println("加载训练数据: " + trainingData.size() + " 条记录");
                processor.buildClassifier(trainingData);
                System.out.println("分类器训练完成");
                
                // 保存模型
                System.out.println("保存模型...");
                processor.saveModel();
                System.out.println("模型保存完成");
            } else {
                // 如果模型存在，直接加载
                System.out.println("加载已有模型...");
                processor.loadModel();
                System.out.println("模型加载完成");
            }
            
            // 测试用例
            String[] testQueries = {
                "在用户管理系统中增加权限分配功能,管理员可以通过页面操作管理权限",
                "创建一个新的数据表用于存储客户信息",
                "实现一个用户登录界面，包含用户名密码输入框和登录按钮",
                "设计员工信息管理的数据库表结构",
                "创建一个支持拖拽的自定义仪表盘页面",
                "实现用户注册流程，包括邮箱验证和手机号验证",
                "开发一个文件上传组件，支持图片预览和大小限制",
                "实现一个工作流引擎，支持自定义审批流程",
                "创建数据库视图用于统计月度销售数据",
                "开发一个富文本编辑器组件",
                "实现一个动态表单生成器，支持拖拽配置",
                "新增博客实体的置顶状态字段"
            };
            
            // 进行预测
            System.out.println("\n测试预测结果:");
            for (String query : testQueries) {
                List<String> predictedLabels = processor.predict(query);
                System.out.println("\n查询文本: " + query);
                System.out.println("预测标签: " + predictedLabels);
            }
            
        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}