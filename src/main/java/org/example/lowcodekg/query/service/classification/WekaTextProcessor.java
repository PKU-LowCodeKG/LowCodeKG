//package org.example.lowcodekg.query.service.classification;
//
//import org.example.lowcodekg.query.model.TextClassificationData;
//import weka.classifiers.Classifier;
//import weka.classifiers.trees.RandomForest;
//import weka.core.*;
//import weka.filters.Filter;
//import weka.filters.unsupervised.attribute.StringToWordVector;
//
//import java.io.File;
//import java.io.Serializable;
//import java.util.*;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
@Deprecated
public class WekaTextProcessor {
//    // 内部配置类
//    private static class Config {
//        static final String MODEL_SAVE_PATH = "D:\\Master\\Data\\classification\\";
//        static final String MODEL_FILE = MODEL_SAVE_PATH + "text_classifier.model";
//        static final int WORDS_TO_KEEP = 3000;
//        static final int MIN_TERM_FREQ = 1;
//        static final double CONFIDENCE_THRESHOLD = 0.4;
//        static final String RANDOM_FOREST_OPTIONS = "-I 100 -K 0 -depth 0";
//    }
//
//    private Map<String, Classifier> labelClassifiers;
//    private StringToWordVector filter;
//    private Instances dataStructure;
//    private List<String> labels;
//    private double threshold = Config.CONFIDENCE_THRESHOLD;
//    private List<TextClassificationData> trainingData;
//
//    public WekaTextProcessor() {
//        labelClassifiers = new HashMap<>();
//        initializeFilter();
//    }
//
//    private void initializeFilter() {
//        filter = new StringToWordVector();
//        filter.setWordsToKeep(Config.WORDS_TO_KEEP);
//        filter.setOutputWordCounts(true);
//        filter.setTFTransform(true);
//        filter.setIDFTransform(true);
//        filter.setLowerCaseTokens(true);
//        filter.setDoNotOperateOnPerClassBasis(true);
//        filter.setMinTermFreq(Config.MIN_TERM_FREQ);
//        filter.setPeriodicPruning(-1); // 禁用周期性剪枝
//
//        // 禁用文档长度归一化，避免"Average document length is not set!"错误
//        filter.setNormalizeDocLength(new SelectedTag(StringToWordVector.FILTER_NONE, StringToWordVector.TAGS_FILTER));
//
//        // 使用字符级分词器，对中文更友好
//        weka.core.tokenizers.CharacterNGramTokenizer tokenizer = new weka.core.tokenizers.CharacterNGramTokenizer();
//        try {
//            // 设置为1-2gram，捕获单字和双字组合
//            tokenizer.setOptions(weka.core.Utils.splitOptions("-min 1 -max 2"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        filter.setTokenizer(tokenizer);
//
//        // 使用中文停用词表
//        try {
//            weka.core.stopwords.WordsFromFile stopwords = new weka.core.stopwords.WordsFromFile();
//            stopwords.setStopwords(new File(Config.MODEL_SAVE_PATH + "chinese_stopwords.txt"));
//            filter.setStopwordsHandler(stopwords);
//        } catch (Exception e) {
//            System.out.println("警告: 无法加载中文停用词表，将不使用停用词过滤: " + e.getMessage());
//            filter.setStopwordsHandler(new weka.core.stopwords.Null());
//        }
//    }
//
//    public void buildClassifier(List<TextClassificationData> trainingData) throws Exception {
//        this.trainingData = trainingData;
//        prepareDataStructure(trainingData);
//
//        // Create training instances
//        Instances trainInstances = createInstances(trainingData);
//
//        // Apply filter and store the filtered data structure
//        filter.setInputFormat(trainInstances);
//        Instances filteredData = Filter.useFilter(trainInstances, filter);
//
//        // Train a classifier for each label
//        for (String label : labels) {
//            RandomForest classifier = new RandomForest();
//            classifier.setOptions(weka.core.Utils.splitOptions(Config.RANDOM_FOREST_OPTIONS));
//
//            // 为每个标签创建二元分类数据
//            Instances labelData = new Instances(filteredData);
//            for (int i = 0; i < labelData.numInstances(); i++) {
//                TextClassificationData originalItem = trainingData.get(i);
//                boolean hasLabel = originalItem.getLabels().contains(label);
//                labelData.instance(i).setClassValue(hasLabel ? 1.0 : 0.0);
//            }
//
//            classifier.buildClassifier(labelData);
//            labelClassifiers.put(label, classifier);
//        }
//    }
//
//    public List<String> predict(String text) throws Exception {
//        // 创建测试实例（未过滤）
//        Instance instance = createInstance(text);
//
//        // 创建一个包含单个实例的数据集
//        Instances testInstances = new Instances(dataStructure);
//        testInstances.add(instance);
//
//        // 使用已经训练好的过滤器对整个数据集进行过滤
//        Instances filteredInstances;
//        try {
//            filter.setInputFormat(testInstances);
//            filteredInstances = Filter.useFilter(testInstances, filter);
//        } catch (Exception e) {
//            System.err.println("过滤实例时出错: " + e.getMessage());
//            // 尝试使用备用方法
//            filter.input(instance);
//            Instance filteredInstance = filter.output();
//            filteredInstances = new Instances(dataStructure, 1);
//            filteredInstances.add(filteredInstance);
//        }
//
//        if (filteredInstances.numInstances() == 0) {
//            System.err.println("警告: 过滤后没有实例，无法进行预测");
//            return new ArrayList<>();
//        }
//
//        Instance filteredInstance = filteredInstances.firstInstance();
//
//        // 预测每个标签
//        List<String> predictedLabels = new ArrayList<>();
//        System.out.println("预测置信度:");
//
//        for (String label : labels) {
//            Classifier classifier = labelClassifiers.get(label);
//            double[] distribution = classifier.distributionForInstance(filteredInstance);
//            double confidence = distribution[1];
//
//            if (confidence >= threshold) {
//                predictedLabels.add(label);
//            }
//            System.out.printf("  %s: %.2f%%\n", label, confidence * 100);
//        }
//
//        return predictedLabels;
//    }
//
//
//    private void prepareDataStructure(List<TextClassificationData> data) {
//        // Collect unique labels
//        Set<String> uniqueLabels = new HashSet<>();
//        for (TextClassificationData item : data) {
//            uniqueLabels.addAll(item.getLabels());
//        }
//        labels = new ArrayList<>(uniqueLabels);
//        Collections.sort(labels);
//
//        // Create attributes
//        ArrayList<Attribute> attributes = new ArrayList<>();
//        attributes.add(new Attribute("text", (ArrayList<String>) null));
//        attributes.add(new Attribute("class", Arrays.asList("0", "1")));
//
//        // Create data structure
//        dataStructure = new Instances("TextClassification", attributes, 0);
//        dataStructure.setClassIndex(1);
//    }
//
//    private Instance createInstance(String text) {
//        double[] values = new double[2];
//        values[0] = dataStructure.attribute(0).addStringValue(text);
//        values[1] = 0;  // Default class value
//
//        Instance instance = new DenseInstance(1.0, values);
//        instance.setDataset(dataStructure);
//        return instance;
//    }
//
//    private Instances createInstances(List<TextClassificationData> data) {
//        Instances instances = new Instances(dataStructure);
//
//        for (TextClassificationData item : data) {
//            double[] values = new double[2];
//            values[0] = instances.attribute(0).addStringValue(item.getQuery());
//            values[1] = 0;  // Will be set per label in createBinaryLabelData
//            instances.add(new DenseInstance(1.0, values));
//        }
//
//        return instances;
//    }
//
//    // 修改保存模型方法
//    public void saveModel() throws Exception {
//        new File(Config.MODEL_SAVE_PATH).mkdirs();
//
//        // 创建模型包装对象
//        ModelWrapper wrapper = new ModelWrapper(filter, labelClassifiers, labels);
//        weka.core.SerializationHelper.write(Config.MODEL_FILE, wrapper);
//    }
//
//    // 修改加载模型方法
//    public void loadModel() throws Exception {
//        ModelWrapper wrapper = (ModelWrapper) weka.core.SerializationHelper.read(Config.MODEL_FILE);
//
//        this.filter = wrapper.getFilter();
//        this.labelClassifiers = wrapper.getLabelClassifiers();
//        this.labels = wrapper.getLabels();
//
//        // 初始化数据结构
//        ArrayList<Attribute> attributes = new ArrayList<>();
//        attributes.add(new Attribute("text", (ArrayList<String>) null));
//        attributes.add(new Attribute("class", Arrays.asList("0", "1")));
//        dataStructure = new Instances("TextClassification", attributes, 0);
//        dataStructure.setClassIndex(1);
//    }
//
//    // 添加模型评估方法
//    public void evaluateModel() throws Exception {
//        if (trainingData == null) {
//            throw new IllegalStateException("没有训练数据可供评估");
//        }
//
//        System.out.println("\n=== 模型评估结果 ===");
//        for (String label : labels) {
//            // Create and filter instances
//            Instances trainInstances = createInstances(trainingData);
//            filter.setInputFormat(trainInstances);
//            Instances filteredData = Filter.useFilter(trainInstances, filter);
//
//            // Create binary label data
//            Instances labelData = new Instances(filteredData);
//            for (int i = 0; i < labelData.numInstances(); i++) {
//                TextClassificationData originalItem = trainingData.get(i);
//                boolean hasLabel = originalItem.getLabels().contains(label);
//                labelData.instance(i).setClassValue(hasLabel ? 1.0 : 0.0);
//            }
//
//            // Perform cross-validation
//            weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(labelData);
//            eval.crossValidateModel(labelClassifiers.get(label), labelData, 10, new Random(1));
//
//            System.out.printf("\n标签 '%s' 的评估结果:\n", label);
//            System.out.printf("准确率: %.2f%%\n", eval.pctCorrect());
//            System.out.printf("F1值: %.3f\n", eval.weightedFMeasure());
//        }
//    }
//
//    // 内部模型包装类
//    private static class ModelWrapper implements Serializable {
//        private static final long serialVersionUID = 1L;
//        private final StringToWordVector filter;
//        private final Map<String, Classifier> labelClassifiers;
//        private final List<String> labels;
//
//        public ModelWrapper(StringToWordVector filter, Map<String, Classifier> labelClassifiers, List<String> labels) {
//            this.filter = filter;
//            this.labelClassifiers = labelClassifiers;
//            this.labels = labels;
//        }
//
//        public StringToWordVector getFilter() { return filter; }
//        public Map<String, Classifier> getLabelClassifiers() { return labelClassifiers; }
//        public List<String> getLabels() { return labels; }
//    }
//
//    public static void main(String[] args) {
//        try {
//            WekaTextProcessor processor = new WekaTextProcessor();
//            File modelFile = new File(Config.MODEL_FILE);
//
//            // 强制重新训练模型，因为我们修改了过滤器配置
//            if (modelFile.exists()) {
//                System.out.println("删除现有模型以重新训练...");
//                modelFile.delete();
//            }
//
//            if (!modelFile.exists()) {
//                System.out.println("=== 开始模型训练 ===");
//
//                // 加载训练数据
//                String dataPath = "D:\\Master\\Data\\classification\\dataset.json";
//                List<TextClassificationData> trainingData = new Gson().fromJson(
//                    new String(Files.readAllBytes(Paths.get(dataPath))),
//                    new TypeToken<List<TextClassificationData>>(){}.getType()
//                );
//
//                System.out.println("加载训练数据: " + trainingData.size() + " 条记录");
//
//                // 检查标签分布
//                Map<String, Integer> labelCounts = new HashMap<>();
//                for (TextClassificationData item : trainingData) {
//                    for (String label : item.getLabels()) {
//                        labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
//                    }
//                }
//                System.out.println("标签分布:");
//                for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
//                    System.out.printf("  %s: %d 条记录 (%.1f%%)\n",
//                        entry.getKey(),
//                        entry.getValue(),
//                        100.0 * entry.getValue() / trainingData.size());
//                }
//
//                // 检查数据集是否足够大
//                if (trainingData.size() < 50) {
//                    System.out.println("警告: 训练数据集较小，可能影响模型性能");
//                }
//
//                // 检查是否有标签数据不平衡问题
//                int maxCount = Collections.max(labelCounts.values());
//                int minCount = Collections.min(labelCounts.values());
//                if ((double)maxCount / minCount > 5) {
//                    System.out.println("警告: 标签分布严重不平衡，可能影响模型性能");
//                }
//
//                processor.buildClassifier(trainingData);
//                System.out.println("分类器训练完成");
//
//                // 评估模型
//                System.out.println("\n执行模型评估...");
//                processor.evaluateModel();
//
//                // 保存模型
//                System.out.println("\n保存模型...");
//                processor.saveModel();
//                System.out.println("模型保存完成");
//            } else {
//                System.out.println("=== 加载已有模型 ===");
//                processor.loadModel();
//                System.out.println("模型加载完成");
//            }
//
//            // 测试用例
//            String[] testQueries = {
//                "在用户管理系统中增加权限分配功能,管理员可以通过页面操作管理权限",
//                "创建一个新的数据表用于存储客户信息",
//                "设计一个工作流程用于审批请假申请",
//                "实现一个用户登录界面，包含用户名密码输入框和登录按钮",
//                "开发数据统计报表页面，展示销售数据分析结果",
//                "设计员工信息管理的数据库表结构",
//                "创建一个支持拖拽的自定义仪表盘页面",
//                "实现用户注册流程，包括邮箱验证和手机号验证",
//                "设计一个数据库存储过程来处理订单数据",
//                "开发一个文件上传组件，支持图片预览和大小限制",
//                "实现一个工作流引擎，支持自定义审批流程",
//                "创建数据库视图用于统计月度销售数据",
//                "开发一个富文本编辑器组件",
//                "设计API接口用于处理用户认证",
//                "实现一个动态表单生成器，支持拖拽配置"
//            };
//
//            // 进行预测测试
//            System.out.println("\n=== 测试预测结果 ===");
//            for (String query : testQueries) {
//                System.out.println("\n查询文本: " + query);
//                List<String> predictedLabels = processor.predict(query);
//                System.out.println("预测标签: " + String.join(", ", predictedLabels));
//                System.out.println("-------------------");
//            }
//
//        } catch (Exception e) {
//            System.err.println("发生错误: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
}