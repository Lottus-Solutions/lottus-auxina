package br.com.lottus.auxina.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestResult {
    private String testName;
    private String targetEndpoint;
    private boolean success;
    private long durationMillis;
    private int httpStatus;
    private Double targetServiceMemoryUsedMB;
    private String requestPayload;

    private String methodGroupKey;
}
