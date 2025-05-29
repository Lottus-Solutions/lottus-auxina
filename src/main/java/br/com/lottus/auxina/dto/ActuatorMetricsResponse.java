package br.com.lottus.auxina.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActuatorMetricsResponse {
    private String name;
    private List<Measurement> measurements;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Measurement {
        private String statistic;
        private double value;
    }

}
