package com.smart.doctor_appointment_systemqueue.config;

import com.smart.doctor_appointment_systemqueue.model.Doctor;
import com.smart.doctor_appointment_systemqueue.repository.DoctorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedDoctors(DoctorRepository doctorRepository) {
        return args -> {
            if (doctorRepository.count() > 0) {
                return;
            }

            doctorRepository.saveAll(List.of(
                    createDoctor("Dr. Aisha Kapoor", "Cardiology", "09:00", "13:00", "Cardiac Wellness Wing", "12 years"),
                    createDoctor("Dr. Arjun Menon", "Cardiology", "13:15", "17:00", "Advanced Heart OPD", "8 years"),
                    createDoctor("Dr. Rohan Mehta", "Orthopedics", "10:30", "15:30", "Mobility and Sports Unit", "10 years"),
                    createDoctor("Dr. Kavya Sharma", "Orthopedics", "09:45", "17:15", "Joint and Spine Clinic", "9 years"),
                    createDoctor("Dr. Sana Joseph", "Pediatrics", "11:00", "17:00", "Pediatric Care Center", "9 years"),
                    createDoctor("Dr. Vikram Rao", "Neurology", "14:00", "19:00", "Brain and Nerve Clinic", "14 years"),
                    createDoctor("Dr. Neha Verma", "Dermatology", "09:30", "15:30", "Skin and Allergy Studio", "7 years"),
                    createDoctor("Dr. Aditya Nair", "ENT", "10:00", "16:15", "ENT and Sinus Care", "11 years"),
                    createDoctor("Dr. Meera Iyer", "Gynecology", "11:15", "17:00", "Women's Health Department", "13 years"),
                    createDoctor("Dr. Rishi Kulkarni", "General Medicine", "08:45", "15:45", "Primary Care Block", "15 years"),
                    createDoctor("Dr. Pooja Singh", "General Medicine", "09:15", "18:15", "Family Medicine Desk", "6 years")
            ));
        };
    }

    private Doctor createDoctor(
            String name,
            String specialization,
            String availableFrom,
            String availableTo,
            String clinic,
            String experience
    ) {
        Doctor doctor = new Doctor();
        doctor.setDoctorName(name);
        doctor.setSpecialization(specialization);
        doctor.setAvailableFrom(availableFrom);
        doctor.setAvailableTo(availableTo);
        doctor.setClinic(clinic);
        doctor.setExperience(experience);
        return doctor;
    }
}
