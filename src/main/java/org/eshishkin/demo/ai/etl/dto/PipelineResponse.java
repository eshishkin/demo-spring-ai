package org.eshishkin.demo.ai.etl.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PipelineResponse<T> {

    private final Status status;
    private final T data;
    private final Exception ex;

    public static <T> PipelineResponse<T> success(T data) {
        return new PipelineResponse<>(Status.SUCCESS, data, null);
    }

    public static <T> PipelineResponse<T> failure(T data, Exception ex) {
        return new PipelineResponse<>(Status.FAILURE, data, ex);
    }

    public enum Status {
        SUCCESS,
        FAILURE
    }
}
