package org.example.lowcodekg.service;

import org.example.lowcodekg.dao.neo4j.entity.page.PageEntity;

/**
 * 实体功能描述信息生成服务
 */
public interface FunctionalityGenService {

    String generatePageFunctionality(PageEntity pageEntity);

}
