package br.com.lottus.auxina.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MethodTestDTO {
    private String methodName;
    private int totalTests;
    private int failedTests;
    private int successTests;
    private double avarageDurationMillisInGroup;

    private Double avarageMemoryUsageMbInGroup;

    private List<TestResult> individualTestResults;
}
