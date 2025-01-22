package org.eshishkin.demo.ai.etl.dto;

import java.util.Map;

public record TextMessage(String id, String data, Map<String, Object> metadata) {
}
