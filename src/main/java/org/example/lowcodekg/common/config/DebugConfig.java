package org.example.lowcodekg.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Description 
 * @Author Sherloque
 * @Date 2025/3/30 14:57
 */
@Configuration
public class DebugConfig {

    @Value("${app.debug.mode}")
    private boolean debugMode;

    public boolean isDebugMode() {
        return debugMode;
    }
}
