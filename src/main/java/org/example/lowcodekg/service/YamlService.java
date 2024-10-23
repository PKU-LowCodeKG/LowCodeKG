package org.example.lowcodekg.service;

import org.yaml.snakeyaml.Yaml;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.Map;

@Service
public class YamlService {

    public Map<String, Object> loadYaml() {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getResourceAsStream("/data.yaml")) {
            return yaml.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file", e);
        }
    }
}

