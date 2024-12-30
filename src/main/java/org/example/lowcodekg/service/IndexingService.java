package org.example.lowcodekg.service;

import org.springframework.stereotype.Service;

@Service
public interface IndexingService {

    void exportJavaClassMethodToJson(String jsonPath);

    void updateJsonWithDescription(String inputFile, String outputFile);

}
