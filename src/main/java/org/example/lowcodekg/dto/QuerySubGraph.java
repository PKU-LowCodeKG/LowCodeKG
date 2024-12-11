package org.example.lowcodekg.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuerySubGraph {
    private final String query;
    private final List<Long> remainNodeIds = new ArrayList<>();

    public QuerySubGraph(String query, List<Long> remainNodeIds) {
        this.query = query;
        this.remainNodeIds.addAll(remainNodeIds);
    }
}
