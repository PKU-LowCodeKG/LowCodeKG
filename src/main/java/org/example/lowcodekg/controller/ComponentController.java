package org.example.lowcodekg.controller;

import org.example.lowcodekg.dao.neo4j.entity.ComponentEntity;
import org.example.lowcodekg.dao.neo4j.entity.ConfigItemEntity;
import org.example.lowcodekg.schema.entity.Component;
import org.example.lowcodekg.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 响应搜索请求
 */
@CrossOrigin
@RestController
@RequestMapping("/components")
public class ComponentController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/searchComponentsByName")
    synchronized public List<ComponentEntity> searchComponentsByName(@RequestParam String name) {
        return searchService.searchComponentsByName(name);
    }

    @GetMapping("/getConfigItemsByComponentName")
    synchronized public List<ConfigItemEntity> getConfigItemsByComponentName(@RequestParam String name) {
        return searchService.getConfigItemsByComponentName(name);
    }


//    @RequestMapping(value = "/searchComponent", method = {RequestMethod.GET, RequestMethod.POST})
//    synchronized public List<Component> searchComponent(String keyword) {
//
//        return null;
//    }

}
