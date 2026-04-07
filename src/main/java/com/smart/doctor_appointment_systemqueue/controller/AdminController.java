package com.smart.doctor_appointment_systemqueue.controller;

import com.smart.doctor_appointment_systemqueue.dto.AccountProfile;
import com.smart.doctor_appointment_systemqueue.model.Appointment;
import com.smart.doctor_appointment_systemqueue.model.Doctor;
import com.smart.doctor_appointment_systemqueue.model.User;
import com.smart.doctor_appointment_systemqueue.service.AppointmentService;
import com.smart.doctor_appointment_systemqueue.service.DoctorService;
import com.smart.doctor_appointment_systemqueue.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    public AdminController(UserService userService, AppointmentService appointmentService, DoctorService doctorService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getAdminOverview() {
        Map<String, Object> overview = new HashMap<>();

        overview.put("totalUsers", userService.getAllUsers().size());
        overview.put("totalPatients", userService.countPatients());
        overview.put("totalDoctors", userService.countDoctors());
        overview.put("totalAdmins", userService.countAdmins());
        overview.put("totalAppointments", appointmentService.getAllAppointments().size());

        return ResponseEntity.ok(overview);
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<AccountProfile> updateAdminUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequest request
    ) {
        AccountProfile updated = userService.updateAdminRole(userId, request.getRole());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/appointments/{appointmentId}/now-calling")
    public ResponseEntity<Appointment> makeAppointmentNowCalling(
            @PathVariable Long appointmentId
    ) {
        Appointment appointment = appointmentService.markAppointmentNowCalling(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<Map<String, String>> deleteAppointment(
            @PathVariable Long appointmentId
    ) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.ok(Map.of("message", "Appointment deleted by admin"));
    }

    // ============ USER MANAGEMENT ENDPOINTS ============
    /**
     * Get all users with their roles
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("users", userService.getAllUsers());
        response.put("total", userService.getAllUsers().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new user account
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        try {
            AccountProfile created = userService.registerUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("user", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update user information
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String userId,
            @RequestBody UserUpdateRequest updateRequest) {
        try {
            AccountProfile updated = userService.updateUser(
                    userId,
                    updateRequest.getName(),
                    updateRequest.getEmail(),
                    updateRequest.getPhoneNumber()
            );
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User updated successfully");
            response.put("user", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            HttpStatus status = e.getMessage() != null && e.getMessage().contains("not found")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deactivate a user account
     */
    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "User deactivated successfully",
                    "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get user by ID with details
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(Map.of("user", userService.getUser(userId)));
        } catch (Exception e) {
            HttpStatus status = e.getMessage() != null && e.getMessage().contains("not found")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        }
    }

    // ============ PATIENT MANAGEMENT ENDPOINTS ============
    /**
     * Get all registered patients
     */
    @GetMapping("/patients")
    public ResponseEntity<Map<String, Object>> getAllPatients() {
        try {
            List<AccountProfile> allUsers = userService.getAllUsers();
            List<AccountProfile> patients = allUsers.stream()
                    .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase("patient"))
                    .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("patients", patients);
            response.put("total", patients.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get specific patient details
     */
    @GetMapping("/patients/{patientId}")
    public ResponseEntity<Map<String, Object>> getPatientById(@PathVariable Long patientId) {
        try {
            List<AccountProfile> allUsers = userService.getAllUsers();
            Optional<AccountProfile> patient = allUsers.stream()
                    .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase("patient"))
                    .filter(u -> u.getUserId().contains("patient-" + patientId))
                    .findFirst();
            
            if (patient.isPresent()) {
                return ResponseEntity.ok(Map.of("patient", patient.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Patient not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a patient account permanently (for admin only)
     */
    @DeleteMapping("/patients/{patientId}")
    public ResponseEntity<Map<String, String>> deletePatient(@PathVariable Long patientId) {
        try {
            String userId = "patient-" + patientId;
            
            List<AccountProfile> allUsers = userService.getAllUsers();
            boolean patientExists = allUsers.stream()
                    .anyMatch(u -> u.getUserId().equals(userId) && u.getRole().equalsIgnoreCase("patient"));
            
            if (!patientExists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Patient not found with ID: " + patientId));
            }
            
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Patient account deleted successfully",
                    "patientId", patientId.toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to delete patient: " + e.getMessage()));
        }
    }

    // ============ DOCTOR SCHEDULE MANAGEMENT ENDPOINTS ============
    /**
     * Get all doctors with their schedule information
     */
    @GetMapping("/doctors")
    public ResponseEntity<Map<String, Object>> getAllDoctors() {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorService.getAllDoctors();
        response.put("doctors", doctors);
        response.put("total", doctors.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get specific doctor details
     */
    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<Map<String, Object>> getDoctorById(@PathVariable Long doctorId) {
        try {
            List<Doctor> allDoctors = doctorService.getAllDoctors();
            Optional<Doctor> doctor = allDoctors.stream()
                    .filter(d -> d.getDoctorId().equals(doctorId))
                    .findFirst();
            
            if (doctor.isPresent()) {
                return ResponseEntity.ok(Map.of("doctor", doctor.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Doctor not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update doctor schedule (availability times)
     */
    @PatchMapping("/doctors/{doctorId}/schedule")
    public ResponseEntity<Map<String, Object>> updateDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestBody DoctorScheduleRequest scheduleRequest) {
        try {
            List<Doctor> allDoctors = doctorService.getAllDoctors();
            Optional<Doctor> doctorOpt = allDoctors.stream()
                    .filter(d -> d.getDoctorId().equals(doctorId))
                    .findFirst();
            
            if (doctorOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Doctor not found"));
            }
            
            Doctor doctor = doctorOpt.get();
            
            // Validate time format (HH:MM)
            if (!isValidTimeFormat(scheduleRequest.getAvailableFrom()) || 
                !isValidTimeFormat(scheduleRequest.getAvailableTo())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid time format. Use HH:MM (24-hour format)"));
            }
            
            doctor.setAvailableFrom(scheduleRequest.getAvailableFrom());
            doctor.setAvailableTo(scheduleRequest.getAvailableTo());
            
            // Note: You may need to create an updateDoctor method in DoctorService
            Doctor updated = doctorService.updateDoctor(doctorId, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctor schedule updated successfully");
            response.put("doctor", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update doctor full information
     */
    @PutMapping("/doctors/{doctorId}")
    public ResponseEntity<Map<String, Object>> updateDoctorInfo(
            @PathVariable Long doctorId,
            @RequestBody DoctorUpdateRequest updateRequest) {
        try {
            List<Doctor> allDoctors = doctorService.getAllDoctors();
            Optional<Doctor> doctorOpt = allDoctors.stream()
                    .filter(d -> d.getDoctorId().equals(doctorId))
                    .findFirst();
            
            if (doctorOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Doctor not found"));
            }
            
            Doctor doctor = doctorOpt.get();
            
            if (updateRequest.getDoctorName() != null) {
                doctor.setDoctorName(updateRequest.getDoctorName());
            }
            if (updateRequest.getSpecialization() != null) {
                doctor.setSpecialization(updateRequest.getSpecialization());
            }
            if (updateRequest.getClinic() != null) {
                doctor.setClinic(updateRequest.getClinic());
            }
            if (updateRequest.getExperience() != null) {
                doctor.setExperience(updateRequest.getExperience());
            }
            if (updateRequest.getAvailableFrom() != null) {
                doctor.setAvailableFrom(updateRequest.getAvailableFrom());
            }
            if (updateRequest.getAvailableTo() != null) {
                doctor.setAvailableTo(updateRequest.getAvailableTo());
            }
            
            Doctor updated = doctorService.updateDoctor(doctorId, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctor information updated successfully");
            response.put("doctor", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get doctors by specialization
     */
    @GetMapping("/doctors/specialization/{specialization}")
    public ResponseEntity<Map<String, Object>> getDoctorsBySpecialization(
            @PathVariable String specialization) {
        try {
            Optional<Doctor> doctor = doctorService.getDoctorBySpecialization(specialization);
            Map<String, Object> response = new HashMap<>();
            
            if (doctor.isPresent()) {
                response.put("doctor", doctor.get());
            } else {
                response.put("message", "No doctors found with specialization: " + specialization);
                response.put("doctor", null);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============ DELETE ENDPOINTS ============
    /**
     * Delete a user permanently
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        try {
            // userId is in format "type-id" (e.g., "patient-1", "doctor-5", "admin-3")
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "User deleted successfully",
                    "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to delete user: " + e.getMessage()));
        }
    }

    /**
     * Delete a doctor permanently
     */
    @DeleteMapping("/doctors/{doctorId}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long doctorId) {
        try {
            List<Doctor> allDoctors = doctorService.getAllDoctors();
            Optional<Doctor> doctorOpt = allDoctors.stream()
                    .filter(d -> d.getDoctorId().equals(doctorId))
                    .findFirst();
            
            if (doctorOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Doctor not found"));
            }
            
            doctorService.deleteDoctor(doctorId);
            return ResponseEntity.ok(Map.of(
                    "message", "Doctor deleted successfully",
                    "doctorId", doctorId.toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to delete doctor: " + e.getMessage()));
        }
    }

    // ============ HELPER METHODS ============
    private boolean isValidTimeFormat(String time) {
        if (time == null) return false;
        return time.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$");
    }

    public static class RoleUpdateRequest {
        private String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class UserUpdateRequest {
        private String name;
        private String email;
        private String phoneNumber;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class DoctorScheduleRequest {
        private String availableFrom;
        private String availableTo;

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
    }

    public static class DoctorUpdateRequest {
        private String doctorName;
        private String specialization;
        private String clinic;
        private String experience;
        private String availableFrom;
        private String availableTo;

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
    }
}
