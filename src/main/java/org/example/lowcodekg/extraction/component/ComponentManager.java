package org.example.lowcodekg.extraction.component;

public class ComponentManager {
    // 测试、统计已有组件库的信息
    public static void main(String[] args) {        
        // Test Ant
        AntMDExtractor testAnt = new AntMDExtractor();
        testAnt.setDataDir("E:\\test\\ant-design-master"); // 目录替换为对应git仓库地址
        testAnt.parseData();
        // Test Element
        EleMDExtractor testElement = new EleMDExtractor();
        testElement.setDataDir("E:\\test\\element-plus-master"); // 目录替换为对应git仓库地址
        testElement.parseData();
        
        // Print info
        System.out.println("Component Manager: available components' info:");
        
        System.out.println("\nComponent library size: AntDesign / ElementPlus:");
        System.out.println(testAnt.dataList.size() + " " + testElement.dataList.size());

        int CodeDemoNum1 = 0, CodeDemoNum2 = 0;
        int CodeDemoSum1 = 0, CodeDemoSum2 = 0;
        int ConfigNum1 = 0, ConfigNum2 = 0;
        int ConfigSum1 = 0, ConfigSum2 = 0;
        int sameName = 0;

        for (int i = 0; i < testAnt.dataList.size(); i++) {
            RawData data1 = testAnt.dataList.get(i);
            if (data1.getCodeDemos().size() > 0) {
                CodeDemoNum1++;
                CodeDemoSum1 += data1.getCodeDemos().size();
            }
            if (data1.getConfigItems().size() > 0) {
                ConfigNum1++;
                ConfigSum1 += data1.getConfigItems().size();
            }
            else {
                System.err.println("Error: RawData with No ConfigItems: " + data1.getName() + " " + i);
            }
        }
        for (int j = 0; j < testElement.dataList.size(); j++) {
            RawData data2 = testElement.dataList.get(j);
            if (data2.getCodeDemos().size() > 0) {
                CodeDemoNum2++;
                CodeDemoSum2 += data2.getCodeDemos().size();
            }
            if (data2.getConfigItems().size() > 0) {
                ConfigNum2++;
                ConfigSum2 += data2.getConfigItems().size();
            }
            else {
                System.err.println("RawData with No ConfigItems: " + data2.getName() + " " + j);
            }
        }
        
        System.out.println("Component Number with CodeDemo: AntDesign / ElementPlus: " + CodeDemoNum1 + " " + CodeDemoNum2);
        System.out.println("Total CodeDemo Number: AntDesign / ElementPlus: " + CodeDemoSum1 + " " + CodeDemoSum2);
        System.out.println("Component Number with ConfigItem: AntDesign / ElementPlus: " + ConfigNum1 + " " + ConfigNum2);
        System.out.println("Total ConfigItem Number: AntDesign / ElementPlus: " + ConfigSum1 + " " + ConfigSum2);

        for (int i = 0; i < testAnt.dataList.size(); i++) {
            for (int j = 0; j < testElement.dataList.size(); j++) {
                RawData data1 = testAnt.dataList.get(i);
                RawData data2 = testElement.dataList.get(j);

                if (data1.getName().equals(data2.getName())) {
                    sameName++;
                    System.out.println(sameName + ": Components with the same Name: " + data1.getName());
                }
            }
        }
        
        return;
    }

}
