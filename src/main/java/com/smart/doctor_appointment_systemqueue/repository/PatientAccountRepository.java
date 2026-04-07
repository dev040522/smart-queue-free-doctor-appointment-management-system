package com.smart.doctor_appointment_systemqueue.repository;

import com.smart.doctor_appointment_systemqueue.model.PatientAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientAccountRepository extends JpaRepository<PatientAccount, Long> {
    Optional<PatientAccount> findByEmail(String email);

    List<PatientAccount> findAllByOrderByNameAscPatientIdAsc();
}
