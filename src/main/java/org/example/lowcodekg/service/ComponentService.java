package org.example.lowcodekg.service;

import org.example.lowcodekg.dao.neo4j.entity.Component;
import org.example.lowcodekg.dao.neo4j.entity.ConfigItem;
import org.example.lowcodekg.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.dao.neo4j.repository.ConfigItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComponentService {
    @Autowired
    private ComponentRepo componentRepo;

    @Autowired
    private ConfigItemRepo configItemRepo;

    public void relateConfig(Long componentId, Long configItemId) {
        Component component = componentRepo.findById(componentId).orElseThrow();
        ConfigItem configItem = configItemRepo.findById(configItemId).orElseThrow();
        component.getRelateConfigItems().add(configItem);
        componentRepo.save(component);
    }
}
