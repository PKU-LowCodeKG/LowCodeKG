package org.example.lowcodekg.search;

import org.example.lowcodekg.dao.neo4j.entity.Component;

import java.util.List;

public interface SearchService {
    public List<Component> searchComponentsByName(String name);

    public List<Component> getConfigItemsByComponentName(String name);
}
