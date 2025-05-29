package br.com.lottus.auxina.dto;

import jdk.jfr.DataAmount;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ModuleTestDTO {
    private String moduleName;
    private int totalTests;
    private int sucessfulTests;
    private int failedTests;
    private double successPercentage;
    private List<MethodTestDTO> methodTestsResults;

}
