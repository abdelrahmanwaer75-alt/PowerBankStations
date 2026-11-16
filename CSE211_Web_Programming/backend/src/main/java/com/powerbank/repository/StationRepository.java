package com.powerbank.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.powerbank.entity.Station;
import jakarta.persistence.LockModeType;

public interface StationRepository extends JpaRepository<Station, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select station from Station station where station.id = :id")
    Optional<Station> findByIdForUpdate(@Param("id") Long id);
}
