package org.example.lowcodekg.search.impl;

import org.example.lowcodekg.dao.neo4j.entity.ComponentEntity;
import org.example.lowcodekg.dao.neo4j.entity.ConfigItemEntity;
import org.example.lowcodekg.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ComponentRepo neo4jComponentRepo;

    @Override
    public List<ComponentEntity> searchComponentsByName(String name) {
        return neo4jComponentRepo.findByNameContaining(name);
    }

    @Override
    public List<ConfigItemEntity> getConfigItemsByComponentName(String name) {
        return neo4jComponentRepo.findConfigItemsByComponentName(name);
    }
}
