package org.example.lowcodekg.controller;

import org.codehaus.jettison.json.JSONException;
import org.example.lowcodekg.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexingController {

    @Autowired
    private IndexingService indexingService;

    @GetMapping("/exportToJson")
    synchronized public String exportToJson(@RequestParam String jsonPath) throws JSONException {
        indexingService.exportJavaClassMethodToJson(jsonPath);
        return "export success!";
    }

    @PutMapping("/updateJson")
    synchronized public String updateJson(@RequestParam String inputFile,
                                          @RequestParam String outputFile) {
        indexingService.updateJsonWithDescription(inputFile, outputFile);
        return "update success!";
    }
}
