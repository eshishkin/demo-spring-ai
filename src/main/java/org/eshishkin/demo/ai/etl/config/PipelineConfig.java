package org.eshishkin.demo.ai.etl.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
@Validated
@ConfigurationProperties("app.etl")
public class PipelineConfig {

    @NotNull
    private Map<String, StepConfig> steps;

    public StepConfig getStepConfig(String stepName) {
        return steps.get(stepName);
    }

    @Data
    public static class StepConfig {

        @NotBlank
        private String input;

        private String output;

        private String retrySuffix = "_retry";
        private String dlqSuffix = "_dlq";
        private int retryLimit = 3;

        private Map<String, String> additional = new HashMap<>();

        public String getRetryTopic() {
            return input + retrySuffix;
        }

        public String getDeadLetterTopic() {
            return input + dlqSuffix;
        }

        public String getAdditionalProperty(String key) {
            return additional.get(key);
        }
    }
}
