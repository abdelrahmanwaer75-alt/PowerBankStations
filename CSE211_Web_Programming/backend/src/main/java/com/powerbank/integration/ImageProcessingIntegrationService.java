package com.powerbank.integration;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestClient;

@Service
public class ImageProcessingIntegrationService {
    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;

    public ImageProcessingIntegrationService(
            RestClient.Builder builder,
            @Value("${services.image-processing.base-url:http://localhost:8002}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public Map<String, Object> inspect(String kind, MultipartFile file) throws IOException {
        if (!java.util.Set.of("powerbank", "infrastructure", "pcb").contains(kind)) {
            throw new IllegalArgumentException("kind must be powerbank, infrastructure, or pcb");
        }
        if (file.isEmpty() || file.getSize() > MAX_UPLOAD_BYTES) {
            throw new IllegalArgumentException("Image must be non-empty and no larger than 10 MB");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Upload must use an image content type");
        }

        ByteArrayResource image = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return "inspection-image";
            }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
        partHeaders.setContentDispositionFormData("file", "inspection-image");
        body.add("file", new HttpEntity<>(image, partHeaders));
        Map<String, Object> response = client.post()
                .uri("/process/" + kind)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(RESPONSE_TYPE);
        if (response == null) {
            throw new IllegalStateException("Image processing service returned an empty response");
        }
        return response;
    }
}
