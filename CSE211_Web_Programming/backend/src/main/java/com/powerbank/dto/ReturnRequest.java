package com.powerbank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReturnRequest(
        @NotNull @Positive Long rentalId,
        @NotNull @Positive Long stationId) {
}
