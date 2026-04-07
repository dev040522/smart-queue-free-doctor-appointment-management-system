package com.smart.doctor_appointment_systemqueue.controller;

import com.smart.doctor_appointment_systemqueue.model.Doctor;
import com.smart.doctor_appointment_systemqueue.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<Doctor> addDoctor(@RequestBody DoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                doctorService.addDoctor(toDoctor(request))
        );
    }

    @PostMapping("/add")
    public ResponseEntity<Doctor> addDoctorLegacy(@RequestBody DoctorRequest request) {
        return addDoctor(request);
    }

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/all")
    public List<Doctor> getAllDoctorsLegacy() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/specialization/{specialization}")
    public Optional<Doctor> getDoctorBySpecialization(@PathVariable String specialization) {
        return doctorService.getDoctorBySpecialization(specialization);
    }

    private Doctor toDoctor(DoctorRequest request) {
        Doctor doctor = new Doctor();
        doctor.setDoctorName(request.getDoctorName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setAvailableFrom(request.getAvailableFrom());
        doctor.setAvailableTo(request.getAvailableTo());
        doctor.setClinic(request.getClinic());
        doctor.setExperience(request.getExperience());
        return doctor;
    }

    public static class DoctorRequest {
        private String doctorName;
        private String specialization;
        private String availableFrom;
        private String availableTo;
        private String clinic;
        private String experience;

        public String getDoctorName() {
            return doctorName;
        }

        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }

        public String getAvailableFrom() {
            return availableFrom;
        }

        public void setAvailableFrom(String availableFrom) {
            this.availableFrom = availableFrom;
        }

        public String getAvailableTo() {
            return availableTo;
        }

        public void setAvailableTo(String availableTo) {
            this.availableTo = availableTo;
        }

        public String getClinic() {
            return clinic;
        }

        public void setClinic(String clinic) {
            this.clinic = clinic;
        }

        public String getExperience() {
            return experience;
        }

        public void setExperience(String experience) {
            this.experience = experience;
        }
    }
}
