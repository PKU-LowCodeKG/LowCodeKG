package org.example.lowcodekg.controller;

import com.alibaba.fastjson.JSON;
import org.example.lowcodekg.dto.*;
import org.example.lowcodekg.schema.entity.Component;
import org.example.lowcodekg.service.Neo4jGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 响应搜索请求
 */
@CrossOrigin
@RestController
public class Controller {

    @Autowired
    Neo4jGraphService neo4jGraphService;

    @RequestMapping(value = "/searchComponent", method = {RequestMethod.GET, RequestMethod.POST})
    synchronized public List<Component> searchComponent(String keyword) {

        return null;
    }

    @PostMapping("/node")
    synchronized public Neo4jNode node(@RequestParam("id") long id) {
        return neo4jGraphService.getNodeDetail(id);
    }

    @PostMapping("/relationList")
    synchronized public List<Neo4jRelation> relationList(@RequestParam("id") long id) {
        return neo4jGraphService.getRelationList(id);
    }

//    @PostMapping("/codeSearch")
//    synchronized public Neo4jSubGraph codeSearch(@RequestParam("query") String query) {
//        return neo4jGraphService.codeSearch(query);
//    }

    @PostMapping("/codeSearch")
    synchronized public Neo4jSubGraph codeSearch(@RequestParam("query") String query) {
        return neo4jGraphService.searchRelevantGraph(query);
//        return neo4jGraphService.findAddTags(query);
    }

    @PostMapping("/codeGeneration")
    synchronized public CodeGenerationResult codeGeneration(@RequestParam("ids") String ids) {
        System.out.println(JSON.parseArray(ids, Long.class));
        CodeGenerationResult generationResult = new CodeGenerationResult();
        String generatedCode = "hello world";
        generationResult.setGeneratedCode(generatedCode);
        return generationResult;
    }
}
