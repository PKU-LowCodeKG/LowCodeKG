package org.example.lowcodekg.service;

import org.example.lowcodekg.dao.neo4j.entity.ComponentEntity;
import org.example.lowcodekg.dao.neo4j.entity.ConfigItemEntity;
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
        ComponentEntity componentEntity = componentRepo.findById(componentId).orElseThrow();
        ConfigItemEntity configItemEntity = configItemRepo.findById(configItemId).orElseThrow();
        componentEntity.getContainedConfigItemEntities().add(configItemEntity);
        componentRepo.save(componentEntity);
    }
}
