package org.example.lowcodekg.controller;

import org.example.lowcodekg.dao.neo4j.entity.Project;
import org.example.lowcodekg.dto.Neo4jNode;
import org.example.lowcodekg.dto.Neo4jRelation;
import org.example.lowcodekg.schema.entity.Component;
import org.example.lowcodekg.service.Neo4jGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @RequestMapping(value = "/projects", method = {RequestMethod.GET})
    public List<Project> searchProjects(){
        List<Project> projects = new ArrayList<>();
        projects.add(new Project("name1", "123"));
        projects.add(new Project("name2", "123456"));

        return projects;
    }

    @PostMapping("/node")
    synchronized public Neo4jNode node(@RequestParam("id") long id, @RequestParam("project") String project) {
        return neo4jGraphService.getNodeDetail(id);
    }

    @PostMapping("/relationList")
    synchronized public List<Neo4jRelation> relationList(@RequestParam("id") long id,
                                                         @RequestParam("project") String project) {
        return neo4jGraphService.getRelationList(id);
    }

}
