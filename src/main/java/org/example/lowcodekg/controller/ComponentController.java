package org.example.lowcodekg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.lowcodekg.dao.neo4j.entity.GraphData;
import org.example.lowcodekg.dao.neo4j.entity.Link;
import org.example.lowcodekg.service.ComponentService;
import org.springframework.web.client.RestTemplate;
import org.example.lowcodekg.dao.neo4j.entity.Node;

/**
 * 响应搜索请求
 */
@CrossOrigin
@RestController
@RequestMapping("/components")
public class ComponentController {

//    @Autowired
//    private SearchService searchService;
//
//    @GetMapping("/searchComponentsByName")
//    synchronized public List<ComponentEntity> searchComponentsByName(@RequestParam String name) {
//        return searchService.searchComponentsByName(name);
//    }
//
//    @GetMapping("/getConfigItemsByComponentName")
//    synchronized public List<ConfigItemEntity> getConfigItemsByComponentName(@RequestParam String name) {
//        return searchService.getConfigItemsByComponentName(name);
//    }

    @Autowired
    private ComponentService componentService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/component/{componentId}/relateConfig/{configItemId}")
    public void relateConfig(@PathVariable Long componentId, @PathVariable Long configItemId) {
        componentService.relateConfig(componentId, configItemId);
    }

    @PostMapping(consumes = "application/json", path = "/importJson")
    public void likeMovie(@RequestBody GraphData graphData) {
        for (Node node : graphData.getNodes()) {
            if ("Component".equals(node.getLabel())) {
                // 调用 http://localhost:8080/component
                restTemplate.postForObject("http://localhost:8080/component", node, Void.class);
            } else if ("ConfigItem".equals(node.getLabel())) {
                // 调用 http://localhost:8080/configItem
                restTemplate.postForObject("http://localhost:8080/configItem", node, Void.class);
            }
        }

        // 处理 links
        for (Link link : graphData.getLinks()) {
            Long componentId = link.getSource();
            Long configItemId = link.getTarget();
            componentService.relateConfig(componentId, configItemId);
        }
    }


//    @RequestMapping(value = "/searchComponent", method = {RequestMethod.GET, RequestMethod.POST})
//    synchronized public List<Component> searchComponent(String keyword) {
//
//        return null;
//    }
}
