package com.powerbank.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import com.powerbank.entity.PowerBank;
import jakarta.persistence.LockModeType;

public interface PowerBankRepository extends JpaRepository<PowerBank, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PowerBank> findFirstByStation_IdAndStatusOrderByIdAsc(Long stationId, String status);
}
