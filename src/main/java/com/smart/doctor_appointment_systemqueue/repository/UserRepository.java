package com.smart.doctor_appointment_systemqueue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smart.doctor_appointment_systemqueue.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    java.util.List<User> findAllByOrderByRoleAscNameAscUserIdAsc();
}
