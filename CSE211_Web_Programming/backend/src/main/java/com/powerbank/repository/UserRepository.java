package com.powerbank.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.powerbank.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
}
