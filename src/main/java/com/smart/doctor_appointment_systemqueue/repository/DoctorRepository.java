package com.smart.doctor_appointment_systemqueue.repository;

import com.smart.doctor_appointment_systemqueue.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {

    Optional<Doctor> findFirstBySpecializationIgnoreCaseOrderByDoctorNameAsc(String specialization);

    boolean existsByDoctorNameIgnoreCaseAndClinicIgnoreCase(String doctorName, String clinic);

    boolean existsByDoctorNameIgnoreCaseAndClinicIgnoreCaseAndDoctorIdNot(
            String doctorName,
            String clinic,
            Long doctorId
    );

    List<Doctor> findAllByOrderBySpecializationAscDoctorNameAsc();
}
