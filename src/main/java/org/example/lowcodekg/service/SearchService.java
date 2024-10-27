package org.example.lowcodekg.service;

import org.example.lowcodekg.dao.neo4j.entity.ComponentEntity;

import java.util.List;

public interface SearchService {
    public List<ComponentEntity> searchComponentsByName(String name);

    public List<ComponentEntity> getConfigItemsByComponentName(String name);
}
