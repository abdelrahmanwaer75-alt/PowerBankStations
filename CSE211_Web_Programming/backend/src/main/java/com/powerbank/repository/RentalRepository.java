package com.powerbank.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.powerbank.entity.Rental;
import jakarta.persistence.LockModeType;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rental from Rental rental where rental.id = :id")
    Optional<Rental> findByIdForUpdate(@Param("id") Long id);
}
