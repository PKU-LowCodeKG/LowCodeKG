package org.example.lowcodekg.query.service.processor;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.Node;

import java.util.List;

/**
 * 响应用户需求，执行资源推荐的流程
 */
public interface MainService {

    /**
     * 给定用户需求，推荐相关资源
     * @param query
     * @return
     */
    Result<List<Node>> recommend(String query);

    /**
     * 给定用户需求列表，推荐相关资源，结果存储到本地
     */
    Result<Void> recommendList(List<String> query, String savePath);
}
