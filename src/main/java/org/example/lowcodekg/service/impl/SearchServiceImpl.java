package org.example.lowcodekg.service.impl;

import org.example.lowcodekg.dao.neo4j.repository.ComponentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl {

    @Autowired
    private ComponentRepo neo4jComponentRepo;

//    @Override
//    public List<ComponentEntity> searchComponentsByName(String name) {
//        return neo4jComponentRepo.findByNameContaining(name);
//    }
//
//    @Override
//    public List<ConfigItemEntity> getConfigItemsByComponentName(String name) {
//        return neo4jComponentRepo.findConfigItemsByComponentName(name);
//    }
}
