package org.example.lowcodekg.dto;

import lombok.Data;

@Data
public class LLMCodeResult {
    private final String codeResult;

    public LLMCodeResult(String codeResult) {
        this.codeResult = codeResult;
    }
}
