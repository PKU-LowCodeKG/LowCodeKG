package org.example.lowcodekg.search;

import org.example.lowcodekg.dao.neo4j.entity.ComponentEntity;
import org.example.lowcodekg.dao.neo4j.entity.ConfigItemEntity;

import java.util.List;

public interface SearchService {
    public List<ComponentEntity> searchComponentsByName(String name);

    public List<ConfigItemEntity> getConfigItemsByComponentName(String name);
}
