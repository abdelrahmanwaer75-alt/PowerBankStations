package com.powerbank.integration;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AIIntegrationService {
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;

    public AIIntegrationService(
            RestClient.Builder builder,
            @Value("${services.ai.base-url:http://localhost:8001}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public Map<String, Object> findRoute(Map<String, Object> request) {
        return post("/find-route", request);
    }

    public Map<String, Object> nearestAvailable(Map<String, Object> request) {
        return post("/nearest-available", request);
    }

    public Map<String, Object> compareAlgorithms(Map<String, Object> request) {
        return post("/compare-algorithms", request);
    }

    private Map<String, Object> post(String path, Map<String, Object> request) {
        Map<String, Object> response = client.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(RESPONSE_TYPE);
        if (response == null) {
            throw new IllegalStateException("AI service returned an empty response");
        }
        return response;
    }
}
