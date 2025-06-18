//package org.example.lowcodekg.query.service.classification;
//
//import org.example.lowcodekg.query.model.TextClassificationData;
//import org.deeplearning4j.nn.graph.ComputationGraph;
//import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
//import org.deeplearning4j.text.tokenization.tokenizerfactory.BertWordPieceTokenizerFactory;
//import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.factory.Nd4j;
//import org.nd4j.linalg.indexing.NDArrayIndex;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.*;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.NavigableMap;
//import java.util.TreeMap;
//
//import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
//import org.deeplearning4j.nn.conf.layers.DenseLayer;
//import org.deeplearning4j.nn.conf.layers.OutputLayer;
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
//import org.nd4j.linalg.activations.Activation;
//import org.nd4j.linalg.learning.config.Adam;
//import org.nd4j.linalg.lossfunctions.LossFunctions;
//
@Deprecated
public class BertTextProcessor {
//    // 内部配置类
//    private static class Config {
//        static final String MODEL_SAVE_PATH = "D:\\Master\\Data\\classification\\";
//        static final String MODEL_FILE = MODEL_SAVE_PATH + "bert_text_classifier.model";
//        static final String BERT_MODEL_PATH = "D:\\Master\\Data\\bert-base-chinese";
//        static final int MAX_SEQUENCE_LENGTH = 128;
//        static final double CONFIDENCE_THRESHOLD = 0.6; // 提高置信度阈值
//        static final int BATCH_SIZE = 16;
//        static final int EPOCHS = 5; // 增加训练轮次
//        static final double LEARNING_RATE = 1e-4; // 调整学习率
//        static final double DROPOUT_RATE = 0.3; // 添加dropout防止过拟合
//    }
//
//    private ComputationGraph model;
//    private BertWordPieceTokenizerFactory tokenizerFactory;
//    private List<String> labels;
//    private double threshold = Config.CONFIDENCE_THRESHOLD;
//    private List<TextClassificationData> trainingData;
//
//    public BertTextProcessor() {
//        initializeTokenizer();
//    }
//
//    private void initializeTokenizer() {
//        try {
//            // 初始化BERT分词器
//            // Load vocabulary from file and create a NavigableMap
//            NavigableMap<String, Integer> vocab = loadVocabulary(new File(Config.BERT_MODEL_PATH + "\\vocab.txt"));
//            tokenizerFactory = new BertWordPieceTokenizerFactory(vocab, true, true);
//        } catch (Exception e) {
//            System.err.println("初始化BERT分词器失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    // Add a method to load vocabulary from file
//    // 修改词汇表加载方法，使用反向排序
//    private NavigableMap<String, Integer> loadVocabulary(File vocabFile) throws IOException {
//    // 使用反向比较器创建TreeMap
//    NavigableMap<String, Integer> vocab = new TreeMap<>(Collections.reverseOrder());
//    try (BufferedReader reader = new BufferedReader(new FileReader(vocabFile))) {
//    String line;
//    int index = 0;
//    while ((line = reader.readLine()) != null) {
//    line = line.trim();
//    if (!line.isEmpty()) {
//    vocab.put(line, index++);
//    }
//    }
//    }
//    System.out.println("加载词汇表完成，共 " + vocab.size() + " 个词条");
//    return vocab;
//    }
//
//    public void buildClassifier(List<TextClassificationData> trainingData) throws Exception {
//        this.trainingData = trainingData;
//
//        // 收集所有唯一标签
//        Set<String> uniqueLabels = new HashSet<>();
//        for (TextClassificationData item : trainingData) {
//            uniqueLabels.addAll(item.getLabels());
//        }
//        labels = new ArrayList<>(uniqueLabels);
//        Collections.sort(labels);
//
//        // 准备训练数据
//        List<String> texts = new ArrayList<>();
//        List<boolean[]> labelVectors = new ArrayList<>();
//
//        for (TextClassificationData item : trainingData) {
//            texts.add(item.getQuery());
//
//            boolean[] labelVector = new boolean[labels.size()];
//            for (int i = 0; i < labels.size(); i++) {
//                labelVector[i] = item.getLabels().contains(labels.get(i));
//            }
//            labelVectors.add(labelVector);
//        }
//
//        // 创建并训练模型
//        createAndTrainModel(texts, labelVectors);
//    }
//
//    private void createAndTrainModel(List<String> texts, List<boolean[]> labelVectors) throws Exception {
//        // 修改为使用DL4J的内置模型加载方式
//        System.out.println("开始创建模型...");
//
//        // 检查BERT模型目录结构
//        File bertDir = new File(Config.BERT_MODEL_PATH);
//        System.out.println("BERT模型目录: " + bertDir.getAbsolutePath());
//        System.out.println("目录存在: " + bertDir.exists());
//        if (bertDir.exists()) {
//            System.out.println("目录内文件:");
//            for (File file : bertDir.listFiles()) {
//                System.out.println(" - " + file.getName());
//            }
//        }
//
//        // 使用更复杂的网络结构和正则化技术
//        org.deeplearning4j.nn.conf.ComputationGraphConfiguration graphConf = new org.deeplearning4j.nn.conf.NeuralNetConfiguration.Builder()
//            .seed(123)
//            .updater(new org.nd4j.linalg.learning.config.Adam(Config.LEARNING_RATE))
//            .weightInit(org.deeplearning4j.nn.weights.WeightInit.XAVIER) // 使用Xavier初始化
//            .l2(1e-4) // 增加L2正则化
//            .graphBuilder()
//            .addInputs("input")
//            .addLayer("dense1", new org.deeplearning4j.nn.conf.layers.DenseLayer.Builder()
//                .nIn(Config.MAX_SEQUENCE_LENGTH)
//                .nOut(512) // 增加网络宽度
//                .activation(org.nd4j.linalg.activations.Activation.RELU)
//                .build(), "input")
//            .addLayer("dropout1", new org.deeplearning4j.nn.conf.layers.DropoutLayer.Builder(Config.DROPOUT_RATE)
//                .build(), "dense1")
//            .addLayer("dense2", new org.deeplearning4j.nn.conf.layers.DenseLayer.Builder()
//                .nIn(512)
//                .nOut(256)
//                .activation(org.nd4j.linalg.activations.Activation.RELU)
//                .build(), "dropout1")
//            .addLayer("dropout2", new org.deeplearning4j.nn.conf.layers.DropoutLayer.Builder(Config.DROPOUT_RATE)
//                .build(), "dense2")
//            .addLayer("dense3", new org.deeplearning4j.nn.conf.layers.DenseLayer.Builder()
//                .nIn(256)
//                .nOut(128)
//                .activation(org.nd4j.linalg.activations.Activation.RELU)
//                .build(), "dropout2")
//            .addLayer("output", new org.deeplearning4j.nn.conf.layers.OutputLayer.Builder()
//                .nIn(128)
//                .nOut(labels.size())
//                .activation(org.nd4j.linalg.activations.Activation.SIGMOID)
//                .lossFunction(org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction.XENT)
//                .build(), "dense3")
//            .setOutputs("output")
//            .build();
//
//        model = new ComputationGraph(graphConf);
//        model.init();
//
//        // 添加早停策略
//        org.deeplearning4j.nn.api.OptimizationAlgorithm optimizationAlgorithm =
//            org.deeplearning4j.nn.api.OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;
//
//        System.out.println("模型创建完成，开始训练...");
//
//        // 准备批次训练数据
//        int numBatches = (texts.size() + Config.BATCH_SIZE - 1) / Config.BATCH_SIZE;
//
//        // 添加数据增强
//        List<String> augmentedTexts = new ArrayList<>(texts);
//        List<boolean[]> augmentedLabels = new ArrayList<>(labelVectors);
//
//        // 简单的数据增强：对每个文本添加一些常见的前缀或后缀
//        String[] prefixes = {"请", "需要", "希望", "要求", "建议"};
//        for (int i = 0; i < texts.size(); i++) {
//            for (String prefix : prefixes) {
//                if (!texts.get(i).startsWith(prefix)) {
//                    augmentedTexts.add(prefix + texts.get(i));
//                    augmentedLabels.add(labelVectors.get(i));
//                }
//            }
//        }
//
//        System.out.println("原始数据量: " + texts.size() + ", 增强后数据量: " + augmentedTexts.size());
//
//        // 使用增强后的数据进行训练
//        for (int epoch = 0; epoch < Config.EPOCHS; epoch++) {
//            System.out.println("Epoch " + (epoch + 1) + "/" + Config.EPOCHS);
//
//            // 每个epoch随机打乱数据
//            List<Integer> indices = new ArrayList<>();
//            for (int i = 0; i < augmentedTexts.size(); i++) {
//                indices.add(i);
//            }
//            Collections.shuffle(indices);
//
//            for (int batchIdx = 0; batchIdx < (augmentedTexts.size() + Config.BATCH_SIZE - 1) / Config.BATCH_SIZE; batchIdx++) {
//                int start = batchIdx * Config.BATCH_SIZE;
//                int end = Math.min(start + Config.BATCH_SIZE, augmentedTexts.size());
//
//                // 准备当前批次的输入和标签
//                INDArray inputFeatures = Nd4j.zeros(end - start, Config.MAX_SEQUENCE_LENGTH);
//                INDArray outputLabels = Nd4j.zeros(end - start, this.labels.size());
//
//                for (int i = start; i < end; i++) {
//                    int idx = indices.get(i);
//                    // 对文本进行分词和转换为特征向量
//                    int[] tokenIds = tokenizeAndConvert(augmentedTexts.get(idx));
//                    int seqLength = Math.min(tokenIds.length, Config.MAX_SEQUENCE_LENGTH);
//
//                    for (int j = 0; j < seqLength; j++) {
//                        inputFeatures.putScalar(new int[]{i - start, j}, tokenIds[j]);
//                    }
//
//                    // 设置标签
//                    for (int j = 0; j < this.labels.size(); j++) {
//                        outputLabels.putScalar(new int[]{i - start, j}, augmentedLabels.get(idx)[j] ? 1.0 : 0.0);
//                    }
//                }
//
//                // 训练模型
//                model.fit(new INDArray[]{inputFeatures}, new INDArray[]{outputLabels});
//
//                if (batchIdx % 10 == 0) {
//                    System.out.printf("Epoch %d/%d, Batch %d/%d 完成\n",
//                        epoch + 1, Config.EPOCHS, batchIdx + 1, (augmentedTexts.size() + Config.BATCH_SIZE - 1) / Config.BATCH_SIZE);
//                }
//            }
//
//            // 每个epoch结束后评估一下模型
//            if (epoch > 0 && epoch % 2 == 0) {
//                System.out.println("Epoch " + (epoch + 1) + " 完成，进行中间评估...");
//                evaluateOnSample(texts, labelVectors, 0.2); // 在20%的原始数据上评估
//            }
//        }
//
//        System.out.println("模型训练完成");
//    }
//
//    // 添加一个在样本数据上评估模型的方法
//    private void evaluateOnSample(List<String> texts, List<boolean[]> labelVectors, double sampleRatio) {
//        int sampleSize = (int)(texts.size() * sampleRatio);
//        if (sampleSize < 1) sampleSize = 1;
//
//        // 随机选择样本
//        List<Integer> indices = new ArrayList<>();
//        for (int i = 0; i < texts.size(); i++) {
//            indices.add(i);
//        }
//        Collections.shuffle(indices);
//
//        int correct = 0;
//        int total = 0;
//
//        for (int i = 0; i < sampleSize; i++) {
//            int idx = indices.get(i);
//            try {
//                List<String> predicted = predict(texts.get(idx), false); // 不打印详细信息
//                boolean[] actual = labelVectors.get(idx);
//
//                for (int j = 0; j < labels.size(); j++) {
//                    boolean isPredicted = predicted.contains(labels.get(j));
//                    boolean isActual = actual[j];
//
//                    if (isPredicted == isActual) {
//                        correct++;
//                    }
//                    total++;
//                }
//            } catch (Exception e) {
//                System.err.println("评估样本时出错: " + e.getMessage());
//            }
//        }
//
//        double accuracy = (double) correct / total;
//        System.out.printf("样本评估准确率: %.2f%%\n", accuracy * 100);
//    }
//
//    // 修改predict方法，添加一个参数控制是否打印详细信息
//    public List<String> predict(String text) throws Exception {
//        return predict(text, true);
//    }
//
//    public List<String> predict(String text, boolean verbose) throws Exception {
//        // 对文本进行分词和转换为特征向量
//        int[] tokenIds = tokenizeAndConvert(text);
//        int seqLength = Math.min(tokenIds.length, Config.MAX_SEQUENCE_LENGTH);
//
//        INDArray inputFeatures = Nd4j.zeros(1, Config.MAX_SEQUENCE_LENGTH);
//
//        for (int i = 0; i < seqLength; i++) {
//            inputFeatures.putScalar(new int[]{0, i}, tokenIds[i]);
//        }
//
//        // 进行预测
//        INDArray predictions = model.output(inputFeatures)[0];
//
//        // 收集预测结果
//        List<String> predictedLabels = new ArrayList<>();
//        if (verbose) {
//            System.out.println("预测置信度:");
//        }
//
//        for (int i = 0; i < labels.size(); i++) {
//            double confidence = predictions.getDouble(0, i);
//
//            if (confidence >= threshold) {
//                predictedLabels.add(labels.get(i));
//            }
//            if (verbose) {
//                System.out.printf("  %s: %.2f%%\n", labels.get(i), confidence * 100);
//            }
//        }
//
//        return predictedLabels;
//    }
//
//    // 保存模型
//    public void saveModel() throws Exception {
//        new File(Config.MODEL_SAVE_PATH).mkdirs();
//
//        // 保存模型
//        File modelFile = new File(Config.MODEL_FILE);
//        model.save(modelFile);
//
//        // 保存标签信息
//        try (ObjectOutputStream oos = new ObjectOutputStream(
//                new FileOutputStream(Config.MODEL_SAVE_PATH + "bert_labels.bin"))) {
//            oos.writeObject(labels);
//        }
//    }
//
//    // 加载模型
//    public void loadModel() throws Exception {
//        // 加载模型
//        model = ComputationGraph.load(new File(Config.MODEL_FILE), false);
//
//        // 加载标签信息
//        try (ObjectInputStream ois = new ObjectInputStream(
//                new FileInputStream(Config.MODEL_SAVE_PATH + "bert_labels.bin"))) {
//            labels = (List<String>) ois.readObject();
//        }
//    }
//
//    // 添加模型评估方法
//    public void evaluateModel() throws Exception {
//        if (trainingData == null) {
//            throw new IllegalStateException("没有训练数据可供评估");
//        }
//
//        System.out.println("\n=== 模型评估结果 ===");
//
//        // 准备评估数据
//        List<String> texts = new ArrayList<>();
//        List<boolean[]> labelVectors = new ArrayList<>();
//
//        for (TextClassificationData item : trainingData) {
//            texts.add(item.getQuery());
//
//            boolean[] labelVector = new boolean[labels.size()];
//            for (int i = 0; i < labels.size(); i++) {
//                labelVector[i] = item.getLabels().contains(labels.get(i));
//            }
//            labelVectors.add(labelVector);
//        }
//
//        // 执行交叉验证
//        int folds = 5;
//        int foldSize = texts.size() / folds;
//
//        double totalAccuracy = 0;
//        double totalF1 = 0;
//
//        for (int fold = 0; fold < folds; fold++) {
//            int startTest = fold * foldSize;
//            int endTest = (fold == folds - 1) ? texts.size() : (fold + 1) * foldSize;
//
//            // 分割训练集和测试集
//            List<String> trainTexts = new ArrayList<>();
//            List<boolean[]> trainLabels = new ArrayList<>();
//            List<String> testTexts = new ArrayList<>();
//            List<boolean[]> testLabels = new ArrayList<>();
//
//            for (int i = 0; i < texts.size(); i++) {
//                if (i >= startTest && i < endTest) {
//                    testTexts.add(texts.get(i));
//                    testLabels.add(labelVectors.get(i));
//                } else {
//                    trainTexts.add(texts.get(i));
//                    trainLabels.add(labelVectors.get(i));
//                }
//            }
//
//            // 训练模型
//            createAndTrainModel(trainTexts, trainLabels);
//
//            // 评估模型
//            int correct = 0;
//            int total = 0;
//            int truePositives = 0;
//            int falsePositives = 0;
//            int falseNegatives = 0;
//
//            for (int i = 0; i < testTexts.size(); i++) {
//                List<String> predicted = predict(testTexts.get(i));
//                boolean[] actual = testLabels.get(i);
//
//                for (int j = 0; j < labels.size(); j++) {
//                    boolean isPredicted = predicted.contains(labels.get(j));
//                    boolean isActual = actual[j];
//
//                    if (isPredicted == isActual) {
//                        correct++;
//                    }
//
//                    if (isPredicted && isActual) {
//                        truePositives++;
//                    } else if (isPredicted && !isActual) {
//                        falsePositives++;
//                    } else if (!isPredicted && isActual) {
//                        falseNegatives++;
//                    }
//
//                    total++;
//                }
//            }
//
//            double accuracy = (double) correct / total;
//            double precision = (double) truePositives / (truePositives + falsePositives);
//            double recall = (double) truePositives / (truePositives + falseNegatives);
//            double f1 = 2 * precision * recall / (precision + recall);
//
//            totalAccuracy += accuracy;
//            totalF1 += f1;
//        }
//
//        double avgAccuracy = totalAccuracy / folds;
//        double avgF1 = totalF1 / folds;
//
//        System.out.printf("平均准确率: %.2f%%\n", avgAccuracy * 100);
//        System.out.printf("平均F1值: %.3f\n", avgF1);
//
//        // 评估每个标签的性能
//        for (int i = 0; i < labels.size(); i++) {
//            String label = labels.get(i);
//
//            int truePositives = 0;
//            int falsePositives = 0;
//            int falseNegatives = 0;
//
//            for (int j = 0; j < texts.size(); j++) {
//                List<String> predicted = predict(texts.get(j));
//                boolean[] actual = labelVectors.get(j);
//
//                boolean isPredicted = predicted.contains(label);
//                boolean isActual = actual[i];
//
//                if (isPredicted && isActual) {
//                    truePositives++;
//                } else if (isPredicted && !isActual) {
//                    falsePositives++;
//                } else if (!isPredicted && isActual) {
//                    falseNegatives++;
//                }
//            }
//
//            double precision = (double) truePositives / (truePositives + falsePositives);
//            double recall = (double) truePositives / (truePositives + falseNegatives);
//            double f1 = 2 * precision * recall / (precision + recall);
//
//            System.out.printf("\n标签 '%s' 的评估结果:\n", label);
//            System.out.printf("精确率: %.3f\n", precision);
//            System.out.printf("召回率: %.3f\n", recall);
//            System.out.printf("F1值: %.3f\n", f1);
//        }
//    }
//
//    // Add this method before it's called in createAndTrainModel
//    private int[] tokenizeAndConvert(String text) {
//        // 使用BERT分词器对文本进行分词
//        List<String> tokens = tokenizerFactory.create(text).getTokens();
//
//        // 添加特殊标记
//        List<String> bertTokens = new ArrayList<>();
//        bertTokens.add("[CLS]");
//        bertTokens.addAll(tokens);
//        bertTokens.add("[SEP]");
//
//        // 转换为token ID
//        int[] tokenIds = new int[bertTokens.size()];
//        for (int i = 0; i < bertTokens.size(); i++) {
//            String token = bertTokens.get(i);
//            // 使用分词器的词汇表获取ID
//            Integer id = tokenizerFactory.getVocab().get(token);
//            tokenIds[i] = (id != null) ? id : tokenizerFactory.getVocab().get("[UNK]"); // 使用[UNK]标记未知词
//        }
//
//        return tokenIds;
//    }
//
//    public static void main(String[] args) {
//        try {
//            BertTextProcessor processor = new BertTextProcessor();
//            File modelFile = new File(Config.MODEL_FILE);
//            File labelFile = new File(Config.MODEL_SAVE_PATH + "bert_labels.bin");
//
//            // 检查是否存在模型文件，如果存在则删除
//            if (modelFile.exists()) {
//                System.out.println("发现已有模型文件，正在删除...");
//                modelFile.delete();
//                if (labelFile.exists()) {
//                    labelFile.delete();
//                }
//                System.out.println("已删除旧模型文件，将重新训练");
//            }
//
//            System.out.println("=== 开始模型训练 ===");
//
//            // 加载训练数据
//            String dataPath = "D:\\Master\\Data\\classification\\dataset.json";
//            List<TextClassificationData> trainingData = new Gson().fromJson(
//                new String(Files.readAllBytes(Paths.get(dataPath))),
//                new TypeToken<List<TextClassificationData>>(){}.getType()
//            );
//
//            System.out.println("加载训练数据: " + trainingData.size() + " 条记录");
//
//            // 检查标签分布
//            Map<String, Integer> labelCounts = new HashMap<>();
//            for (TextClassificationData item : trainingData) {
//                for (String label : item.getLabels()) {
//                    labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
//                }
//            }
//            System.out.println("标签分布:");
//            for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
//                System.out.printf("  %s: %d 条记录 (%.1f%%)\n",
//                    entry.getKey(),
//                    entry.getValue(),
//                    100.0 * entry.getValue() / trainingData.size());
//            }
//
//            processor.buildClassifier(trainingData);
//            System.out.println("分类器训练完成");
//
//            // 评估模型
//            System.out.println("\n执行模型评估...");
//            processor.evaluateModel();
//
//            // 保存模型
//            System.out.println("\n保存模型...");
//            processor.saveModel();
//            System.out.println("模型保存完成");
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