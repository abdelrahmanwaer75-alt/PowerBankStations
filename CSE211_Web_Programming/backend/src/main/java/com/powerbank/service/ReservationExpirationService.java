package com.powerbank.service;

import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.powerbank.entity.Reservation;
import com.powerbank.repository.PowerBankRepository;
import com.powerbank.repository.ReservationRepository;

@Service
public class ReservationExpirationService {
    private final ReservationRepository reservations;
    private final PowerBankRepository powerBanks;

    public ReservationExpirationService(
            ReservationRepository reservations,
            PowerBankRepository powerBanks) {
        this.reservations = reservations;
        this.powerBanks = powerBanks;
    }

    @Scheduled(fixedDelayString = "${reservations.expiration-poll-ms:30000}")
    @Transactional
    public void expireReservations() {
        List<Reservation> expired =
                reservations.findByStatusAndExpiresAtLessThanEqual("ACTIVE", Instant.now());
        for (Reservation reservation : expired) {
            reservation.status = "EXPIRED";
            if (reservation.powerBank != null && "RESERVED".equals(reservation.powerBank.status)) {
                reservation.powerBank.status = "AVAILABLE";
                powerBanks.save(reservation.powerBank);
            }
        }
        reservations.saveAll(expired);
    }
}
