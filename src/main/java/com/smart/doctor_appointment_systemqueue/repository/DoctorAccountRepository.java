package com.smart.doctor_appointment_systemqueue.repository;

import com.smart.doctor_appointment_systemqueue.model.DoctorAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorAccountRepository extends JpaRepository<DoctorAccount, Long> {
    Optional<DoctorAccount> findByEmail(String email);

    List<DoctorAccount> findAllByOrderByNameAscDoctorAccountIdAsc();
}
