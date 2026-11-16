package com.powerbank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.Instant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powerbank.entity.PowerBank;
import com.powerbank.entity.Reservation;
import com.powerbank.entity.Station;
import com.powerbank.entity.User;
import com.powerbank.repository.PowerBankRepository;
import com.powerbank.repository.ReservationRepository;
import com.powerbank.repository.StationRepository;
import com.powerbank.repository.UserRepository;
import com.powerbank.service.ReservationExpirationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository users;

    @Autowired
    private StationRepository stations;

    @Autowired
    private PowerBankRepository powerBanks;

    @Autowired
    private ReservationRepository reservations;

    @Autowired
    private ReservationExpirationService expirationService;

    @Test
    void registrationHashesPasswordAndLoginVerifiesIt() throws Exception {
        String email = "test-" + java.util.UUID.randomUUID() + "@example.com";
        String registration = """
                {"name":"Test Customer","email":"%s","phone":"01000000000","password":"SecurePass123!"}
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"SecurePass123!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"wrong"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reservationCanStartAndReturnOnlyOnce() throws Exception {
        User user = users.save(newUser());
        Station pickupStation = stations.save(newStation("Pickup"));
        Station returnStation = stations.save(newStation("Return"));
        PowerBank powerBank = new PowerBank();
        powerBank.serialNumber = java.util.UUID.randomUUID().toString();
        powerBank.station = pickupStation;
        powerBank = powerBanks.save(powerBank);

        String reservationResponse = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d,"stationId":%d}
                                """.formatted(user.id, pickupStation.id)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode reservationJson = objectMapper.readTree(reservationResponse);
        long reservationId = reservationJson.get("id").asLong();
        String pickupCode = reservationJson.get("pickupCode").asText();
        assertEquals("RESERVED", powerBanks.findById(powerBank.id).orElseThrow().status);

        mockMvc.perform(post("/api/rentals/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reservationId":%d,"pickupCode":"%s"}
                                """.formatted(reservationId, pickupCode)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/rentals/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reservationId":%d,"pickupCode":"%s"}
                                """.formatted(reservationId, pickupCode)))
                .andExpect(status().isConflict());

        String activeRentals = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/rentals/active"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long rentalId = objectMapper.readTree(activeRentals).get(0).get("id").asLong();

        mockMvc.perform(post("/api/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rentalId":%d,"stationId":%d}
                                """.formatted(rentalId, returnStation.id)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rentalId":%d,"stationId":%d}
                                """.formatted(rentalId, returnStation.id)))
                .andExpect(status().isConflict());
        assertEquals("AVAILABLE", powerBanks.findById(powerBank.id).orElseThrow().status);
    }

    @Test
    void expiredReservationReleasesItsPowerBank() {
        User user = users.save(newUser());
        Station station = stations.save(newStation("Expired"));
        PowerBank powerBank = new PowerBank();
        powerBank.serialNumber = java.util.UUID.randomUUID().toString();
        powerBank.status = "RESERVED";
        powerBank.station = station;
        powerBank = powerBanks.save(powerBank);
        Reservation reservation = new Reservation();
        reservation.user = user;
        reservation.station = station;
        reservation.powerBank = powerBank;
        reservation.expiresAt = Instant.now().minusSeconds(1);
        reservation = reservations.save(reservation);

        expirationService.expireReservations();

        assertEquals("EXPIRED", reservations.findById(reservation.id).orElseThrow().status);
        assertEquals("AVAILABLE", powerBanks.findById(powerBank.id).orElseThrow().status);
    }

    private User newUser() {
        User user = new User();
        user.name = "Workflow User";
        user.email = java.util.UUID.randomUUID() + "@example.com";
        user.password = "test-hash";
        return user;
    }

    private Station newStation(String name) {
        Station station = new Station();
        station.name = name;
        station.capacity = 1;
        return station;
    }
}
