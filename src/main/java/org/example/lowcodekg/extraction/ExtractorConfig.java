package org.example.lowcodekg.extraction;

import lombok.Getter;

import java.util.List;

public class ExtractorConfig {

    @Getter
    private String className;
    @Getter
    private List<String> dataDir;

    public ExtractorConfig(String className, List<String> dataDir) {
        this.className = className;
        this.dataDir = dataDir;
    }
}
