package com.powerbank.service;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.powerbank.entity.Rental;
import com.powerbank.entity.Reservation;
import com.powerbank.repository.PowerBankRepository;
import com.powerbank.repository.RentalRepository;
import com.powerbank.repository.ReservationRepository;

@Service
public class RentalService {
    private final ReservationRepository reservations;
    private final RentalRepository rentals;
    private final PowerBankRepository banks;

    public RentalService(ReservationRepository reservations, RentalRepository rentals, PowerBankRepository banks) {
        this.reservations = reservations;
        this.rentals = rentals;
        this.banks = banks;
    }

    @Transactional
    public Rental start(Long reservationId, String code) {
        Reservation reservation = reservations.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Reservation not found"));
        if (code == null || reservation.pickupCode == null
                || !"ACTIVE".equals(reservation.status)
                || reservation.expiresAt == null
                || !reservation.expiresAt.isAfter(Instant.now())
                || !reservation.pickupCode.equalsIgnoreCase(code.trim())) {
            throw new IllegalStateException("Invalid, expired, or inactive reservation code");
        }

        Rental rental = new Rental();
        rental.user = reservation.user;
        rental.powerBank = reservation.powerBank;
        rental.reservation = reservation;
        rental.startTime = Instant.now();
        reservation.status = "USED";
        reservation.powerBank.status = "RENTED";
        banks.save(reservation.powerBank);
        reservations.save(reservation);
        return rentals.save(rental);
    }

    public List<Rental> all() {
        return rentals.findAll();
    }
}
