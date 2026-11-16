package com.powerbank.dto;

import jakarta.validation.constraints.NotBlank;

public record RouteRequest(
        @NotBlank String start,
        @NotBlank String goal,
        String algorithm) {
}
