package com.smart.doctor_appointment_systemqueue.service;

import com.smart.doctor_appointment_systemqueue.model.Doctor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DoctorServiceTests {

    @Autowired
    private DoctorService doctorService;

    @Test
    void updateDoctorAllowsEditingExistingDoctorWithoutDuplicateConflict() {
        Doctor doctor = new Doctor();
        doctor.setDoctorName("Dr. Test Admin");
        doctor.setSpecialization("Cardiology");
        doctor.setAvailableFrom("09:00");
        doctor.setAvailableTo("13:00");
        doctor.setClinic("North Wing");
        doctor.setExperience("8 years");

        Doctor savedDoctor = doctorService.addDoctor(doctor);

        Doctor update = new Doctor();
        update.setDoctorName("Dr. Test Admin");
        update.setSpecialization("Cardiology");
        update.setAvailableFrom("10:00");
        update.setAvailableTo("14:30");
        update.setClinic("North Wing");
        update.setExperience("9 years");

        Doctor updatedDoctor = doctorService.updateDoctor(savedDoctor.getDoctorId(), update);

        assertEquals(savedDoctor.getDoctorId(), updatedDoctor.getDoctorId());
        assertEquals("10:00", updatedDoctor.getAvailableFrom());
        assertEquals("14:30", updatedDoctor.getAvailableTo());
        assertEquals("9 years", updatedDoctor.getExperience());
    }
}
