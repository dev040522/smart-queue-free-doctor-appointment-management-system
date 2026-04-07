package com.smart.doctor_appointment_systemqueue.controller;

import com.smart.doctor_appointment_systemqueue.model.Appointment;
import com.smart.doctor_appointment_systemqueue.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentContoller {
    private final AppointmentService appointmentService;

    public AppointmentContoller(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody AppointmentRequest request) {
        Appointment appointment = appointmentService.createAppointment(
                request.getDoctorId(),
                request.getUserName(),
                request.getUserEmail(),
                request.getSymptoms(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                request.getConsultationType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @PostMapping("/request")
    public ResponseEntity<Appointment> createAppointmentLegacy(@RequestBody AppointmentRequest request) {
        return createAppointment(request);
    }

    @GetMapping("/queue")
    public List<QueueEntryResponse> getTodayQueue() {
        List<Appointment> appointments = appointmentService.getTodayQueue();

        return appointments.stream()
                .map(appointment -> new QueueEntryResponse(
                        appointment.getId(),
                        appointment.getUserName(),
                        appointment.getDoctorName(),
                        appointment.getSpecialization(),
                        appointment.getAppointmentTime(),
                        appointment.getTokenNumber(),
                        appointment.getStatus()
                ))
                .toList();
    }

    @GetMapping
    public List<Appointment> getAllAppointments(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String appointmentDate
    ) {
        if (userEmail != null && !userEmail.isBlank()) {
            return appointmentService.getAppointmentsForUser(userEmail);
        }

        if (doctorName != null && !doctorName.isBlank()) {
            return appointmentService.getAppointmentsForDoctor(doctorName, appointmentDate);
        }

        return appointmentService.getAllAppointments();
    }

    @GetMapping("/all")
    public List<Appointment> getAllAppointmentsLegacy() {
        return appointmentService.getAllAppointments();
    }

    @PatchMapping("/{id}/status")
    public Appointment updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        return appointmentService.updateAppointmentStatus(id, request.getStatus());
    }

    public static class AppointmentRequest {
        private Long doctorId;
        private String userName;
        private String userEmail;
        private String symptoms;
        private String appointmentDate;
        private String appointmentTime;
        private String consultationType;

        public Long getDoctorId() {
            return doctorId;
        }

        public void setDoctorId(Long doctorId) {
            this.doctorId = doctorId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getSymptoms() {
            return symptoms;
        }

        public void setSymptoms(String symptoms) {
            this.symptoms = symptoms;
        }

        public String getAppointmentDate() {
            return appointmentDate;
        }

        public void setAppointmentDate(String appointmentDate) {
            this.appointmentDate = appointmentDate;
        }

        public String getAppointmentTime() {
            return appointmentTime;
        }

        public void setAppointmentTime(String appointmentTime) {
            this.appointmentTime = appointmentTime;
        }

        public String getConsultationType() {
            return consultationType;
        }

        public void setConsultationType(String consultationType) {
            this.consultationType = consultationType;
        }
    }

    public static class StatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class QueueEntryResponse {
        private final Long id;
        private final String name;
        private final String doctorName;
        private final String specialization;
        private final String appointmentTime;
        private final int tokenNumber;
        private final String status;

        public QueueEntryResponse(
                Long id,
                String name,
                String doctorName,
                String specialization,
                String appointmentTime,
                int tokenNumber,
                String status
        ) {
            this.id = id;
            this.name = name;
            this.doctorName = doctorName;
            this.specialization = specialization;
            this.appointmentTime = appointmentTime;
            this.tokenNumber = tokenNumber;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public String getSpecialization() {
            return specialization;
        }

        public String getAppointmentTime() {
            return appointmentTime;
        }

        public int getTokenNumber() {
            return tokenNumber;
        }

        public String getStatus() {
            return status;
        }
    }
}
