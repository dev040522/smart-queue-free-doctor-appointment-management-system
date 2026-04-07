package com.smart.doctor_appointment_systemqueue.repository;

import com.smart.doctor_appointment_systemqueue.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    int countByDoctorName(String doctorName);

    int countByDoctorNameAndAppointmentDate(String doctorName, String appointmentDate);

    Optional<Appointment> findTopByDoctorNameAndAppointmentDateOrderByTokenNumberDesc(
            String doctorName,
            String appointmentDate
    );

    boolean existsByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
            String doctorName,
            String appointmentDate,
            String appointmentTime,
            Collection<String> statuses
    );

    List<Appointment> findAllByAppointmentDateOrderByAppointmentTimeAscTokenNumberAsc(String appointmentDate);

    List<Appointment> findAllByOrderByAppointmentDateDescAppointmentTimeAscTokenNumberAsc();

    List<Appointment> findAllByUserEmailOrderByAppointmentDateDescAppointmentTimeDescIdDesc(String userEmail);

    List<Appointment> findAllByDoctorNameOrderByAppointmentDateDescAppointmentTimeAscTokenNumberAsc(String doctorName);

    List<Appointment> findAllByDoctorNameAndAppointmentDateOrderByAppointmentTimeAscTokenNumberAsc(
            String doctorName,
            String appointmentDate
    );
}
