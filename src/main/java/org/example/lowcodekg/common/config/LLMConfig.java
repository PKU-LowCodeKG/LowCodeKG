package org.example.lowcodekg.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {
    @Value("${llm.model.type}")
    private String modelType;

    public String getModelType() {
        return modelType;
    }
}