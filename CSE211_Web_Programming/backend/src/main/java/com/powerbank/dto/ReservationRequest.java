package com.powerbank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long stationId) {
}
