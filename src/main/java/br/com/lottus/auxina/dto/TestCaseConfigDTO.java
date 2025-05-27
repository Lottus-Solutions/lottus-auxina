package br.com.lottus.auxina.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class TestCaseConfigDTO {
    private String testName;
    private String targetService;
    private String httpMethod;
    private String endpoint;
    private Map<String, Object> requestBodyTemplate;
    private Map<String, String> queryParamsTemplate;
    private ScenarioType scenarioType;
    private int expectedHtppStatus;

    private String expectedResponseBodyPattern;
    private String expectedErrorCode;



}

