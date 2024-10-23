package org.example.lowcodekg.controller;

import org.example.lowcodekg.dao.neo4j.entity.Project;
import org.example.lowcodekg.schema.entity.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应搜索请求
 */
@CrossOrigin
@RestController
public class Controller {

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

}
