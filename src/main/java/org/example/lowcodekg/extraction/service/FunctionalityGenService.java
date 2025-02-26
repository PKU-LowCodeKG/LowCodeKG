package org.example.lowcodekg.extraction.service;

import org.example.lowcodekg.dao.neo4j.entity.page.PageEntity;

public interface FunctionalityGenService {

    /**
     * 针对单个页面，生成功能描述信息
     */
    void generatePageFunctionality(PageEntity pageEntity);

}
