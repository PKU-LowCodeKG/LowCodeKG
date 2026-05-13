package org.example.lowcodekg.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.model.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.service.FunctionalityGenService;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FunctionalityGenServiceImpl implements FunctionalityGenService {

    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private WorkflowRepo workflowRepo;
    @Autowired
    private ComponentRepo componentRepo;

    @Override
    public void genWorkflowModule() {
        // LLM call removed — workflow module classification disabled
    }


    public static void main(String[] args) {
        String str = """
                ```json
                                    {
                                        "功能概括": "",
                                        "执行逻辑": "",
                                        "技术特征": ""
                                    }
                                    ```
                """;

        Pattern p = Pattern.compile("```json\\s*(\\{[.\\d\\w\\s\\n\\D]*\\})\\s*```");
        Matcher m = p.matcher(str);
        while (m.find()) {
            System.out.println(m.group(1));
        }
    }
}
