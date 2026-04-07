package com.smart.doctor_appointment_systemqueue.service;

import com.smart.doctor_appointment_systemqueue.exception.BadRequestException;
import com.smart.doctor_appointment_systemqueue.exception.ConflictException;
import com.smart.doctor_appointment_systemqueue.model.Appointment;
import com.smart.doctor_appointment_systemqueue.model.Doctor;
import com.smart.doctor_appointment_systemqueue.repository.AppointmentRepository;
import com.smart.doctor_appointment_systemqueue.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AppointmentServiceTests {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        testDoctor = doctorRepository.findAllByOrderBySpecializationAscDoctorNameAsc().stream()
                .findFirst()
                .orElseThrow();
    }

    @Test
    void createAppointmentRejectsPastDates() {
        assertThrows(BadRequestException.class, () -> appointmentService.createAppointment(
                testDoctor.getDoctorId(),
                "Past Patient",
                "past@example.com",
                "Review",
                LocalDate.now().minusDays(1).toString(),
                "09:00",
                "Hospital Visit"
        ));
    }

    @Test
    void createAppointmentRejectsTimeOutsideDoctorAvailability() {
        assertThrows(BadRequestException.class, () -> appointmentService.createAppointment(
                testDoctor.getDoctorId(),
                "Late Patient",
                "late@example.com",
                "Review",
                LocalDate.now().plusDays(1).toString(),
                "23:00",
                "Hospital Visit"
        ));
    }

    @Test
    void createAppointmentRejectsDuplicateSlotBookings() {
        String appointmentDate = LocalDate.now().plusDays(1).toString();

        appointmentService.createAppointment(
                testDoctor.getDoctorId(),
                "First Patient",
                "first@example.com",
                "Consultation",
                appointmentDate,
                testDoctor.getAvailableFrom(),
                "Hospital Visit"
        );

        assertThrows(ConflictException.class, () -> appointmentService.createAppointment(
                testDoctor.getDoctorId(),
                "Second Patient",
                "second@example.com",
                "Consultation",
                appointmentDate,
                testDoctor.getAvailableFrom(),
                "Hospital Visit"
        ));
    }

    @Test
    void updateAppointmentStatusRejectsUnsupportedValues() {
        Appointment appointment = appointmentService.createAppointment(
                testDoctor.getDoctorId(),
                "Status Patient",
                "status@example.com",
                "Consultation",
                LocalDate.now().plusDays(1).toString(),
                testDoctor.getAvailableFrom(),
                "Hospital Visit"
        );

        assertThrows(
                BadRequestException.class,
                () -> appointmentService.updateAppointmentStatus(appointment.getId(), "boarding")
        );
    }

    @Test
    void createAppointmentStoresConsultationTypeAndHigherFeesForOnlineAndHomeVisit() {
        Appointment hospitalVisit = appointmentService.createAppointment(
                testDoctor.getDoctorId(),
                "Hospital Patient",
                "hospital@example.com",
                "Consultation",
                LocalDate.now().plusDays(2).toString(),
                testDoctor.getAvailableFrom(),
                "Hospital Visit"
        );

        Appointment onlineReview = appointmentService.createAppointment(
                testDoctor.getDoctorId(),
                "Online Patient",
                "online@example.com",
                "Consultation",
                LocalDate.now().plusDays(3).toString(),
                testDoctor.getAvailableFrom(),
                "Online Review"
        );

        Appointment homeVisit = appointmentService.createAppointment(
                testDoctor.getDoctorId(),
                "Home Patient",
                "home@example.com",
                "Consultation",
                LocalDate.now().plusDays(4).toString(),
                testDoctor.getAvailableFrom(),
                "Home Visit"
        );

        assertEquals("Hospital Visit", hospitalVisit.getConsultationType());
        assertEquals("Online Review", onlineReview.getConsultationType());
        assertEquals("Home Visit", homeVisit.getConsultationType());
        assertTrue(hospitalVisit.getConsultationFee() < onlineReview.getConsultationFee());
        assertTrue(onlineReview.getConsultationFee() < homeVisit.getConsultationFee());
    }

    @Test
    void adminCanMarkAppointmentNowCalling() {
        Appointment appointment = appointmentService.createAppointment(
                testDoctor.getDoctorId(),
                "Queue Patient",
                "queue@example.com",
                "Follow-up",
                LocalDate.now().plusDays(2).toString(),
                testDoctor.getAvailableFrom(),
                "Hospital Visit"
        );

        Appointment nowCalling = appointmentService.markAppointmentNowCalling(appointment.getId());
        assertEquals("Now calling", nowCalling.getStatus());
    }
}
