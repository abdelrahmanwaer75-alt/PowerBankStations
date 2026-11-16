package com.powerbank.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.powerbank.entity.PowerBank;
import com.powerbank.entity.Reservation;
import com.powerbank.entity.Station;
import com.powerbank.entity.User;
import com.powerbank.repository.PowerBankRepository;
import com.powerbank.repository.ReservationRepository;
import com.powerbank.repository.StationRepository;
import com.powerbank.repository.UserRepository;

@Service
public class ReservationService {
    private final UserRepository users;
    private final StationRepository stations;
    private final PowerBankRepository banks;
    private final ReservationRepository reservations;

    public ReservationService(
            UserRepository users,
            StationRepository stations,
            PowerBankRepository banks,
            ReservationRepository reservations) {
        this.users = users;
        this.stations = stations;
        this.banks = banks;
        this.reservations = reservations;
    }

    @Transactional
    public Reservation create(Long userId, Long stationId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found"));
        Station station = stations.findById(stationId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Station not found"));
        PowerBank powerBank = banks
                .findFirstByStation_IdAndStatusOrderByIdAsc(stationId, "AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("No available PowerBank"));

        powerBank.status = "RESERVED";
        Reservation reservation = new Reservation();
        reservation.user = user;
        reservation.station = station;
        reservation.powerBank = powerBank;
        reservation.pickupCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT);
        banks.save(powerBank);
        return reservations.save(reservation);
    }

    public List<Reservation> all() {
        return reservations.findAll();
    }
}
