package com.smart.doctor_appointment_systemqueue.service;

import com.smart.doctor_appointment_systemqueue.exception.BadRequestException;
import com.smart.doctor_appointment_systemqueue.exception.ConflictException;
import com.smart.doctor_appointment_systemqueue.exception.ResourceNotFoundException;
import com.smart.doctor_appointment_systemqueue.model.Doctor;
import com.smart.doctor_appointment_systemqueue.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService{
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    // Add doctor
    public Doctor addDoctor(Doctor doctor) {
        validateDoctor(doctor);

        String doctorName = doctor.getDoctorName().trim();
        String clinic = doctor.getClinic().trim();

        if (doctorRepository.existsByDoctorNameIgnoreCaseAndClinicIgnoreCase(doctorName, clinic)) {
            throw new ConflictException("Doctor already exists for this clinic");
        }

        doctor.setDoctorName(doctorName);
        doctor.setSpecialization(doctor.getSpecialization().trim());
        doctor.setAvailableFrom(normalizeTime(doctor.getAvailableFrom()));
        doctor.setAvailableTo(normalizeTime(doctor.getAvailableTo()));
        doctor.setClinic(clinic);
        doctor.setExperience(doctor.getExperience().trim());
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long doctorId, Doctor updatedDoctor) {
        if (doctorId == null || doctorId <= 0) {
            throw new BadRequestException("Invalid doctor ID");
        }

        Doctor existingDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        validateDoctor(updatedDoctor);

        String doctorName = updatedDoctor.getDoctorName().trim();
        String clinic = updatedDoctor.getClinic().trim();

        if (doctorRepository.existsByDoctorNameIgnoreCaseAndClinicIgnoreCaseAndDoctorIdNot(
                doctorName,
                clinic,
                doctorId
        )) {
            throw new ConflictException("Doctor already exists for this clinic");
        }

        existingDoctor.setDoctorName(doctorName);
        existingDoctor.setSpecialization(updatedDoctor.getSpecialization().trim());
        existingDoctor.setAvailableFrom(normalizeTime(updatedDoctor.getAvailableFrom()));
        existingDoctor.setAvailableTo(normalizeTime(updatedDoctor.getAvailableTo()));
        existingDoctor.setClinic(clinic);
        existingDoctor.setExperience(updatedDoctor.getExperience().trim());
        return doctorRepository.save(existingDoctor);
    }


    // Get all doctors
    public List<Doctor> getAllDoctors() {

        return doctorRepository.findAllByOrderBySpecializationAscDoctorNameAsc();
    }


    // Get doctor by specialization
    public Optional<Doctor> getDoctorBySpecialization(String specialization) {

        if (specialization == null || specialization.isBlank()) {
            return Optional.empty();
        }

        return doctorRepository.findFirstBySpecializationIgnoreCaseOrderByDoctorNameAsc(
                specialization.trim()
        );
    }

    private void validateDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new BadRequestException("Doctor details are required");
        }

        if (doctor.getDoctorName() == null || doctor.getDoctorName().isBlank()) {
            throw new BadRequestException("Doctor name is required");
        }

        if (doctor.getSpecialization() == null || doctor.getSpecialization().isBlank()) {
            throw new BadRequestException("Specialization is required");
        }

        if (doctor.getClinic() == null || doctor.getClinic().isBlank()) {
            throw new BadRequestException("Clinic is required");
        }

        if (doctor.getExperience() == null || doctor.getExperience().isBlank()) {
            throw new BadRequestException("Experience is required");
        }

        LocalTime availableFrom = parseTime(doctor.getAvailableFrom(), "Available from");
        LocalTime availableTo = parseTime(doctor.getAvailableTo(), "Available to");

        if (!availableTo.isAfter(availableFrom)) {
            throw new BadRequestException("Available to must be after available from");
        }
    }

    private LocalTime parseTime(String time, String fieldName) {
        if (time == null || time.isBlank()) {
            throw new BadRequestException(fieldName + " time is required");
        }

        try {
            return LocalTime.parse(time.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException(fieldName + " time must be in HH:mm format");
        }
    }

    private String normalizeTime(String time) {
        return parseTime(time, "Doctor availability").format(TIME_FORMATTER);
    }

    // Delete doctor
    public void deleteDoctor(Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new BadRequestException("Invalid doctor ID");
        }

        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if (doctor.isEmpty()) {
            throw new BadRequestException("Doctor not found with ID: " + doctorId);
        }

        doctorRepository.deleteById(doctorId);
    }

}
