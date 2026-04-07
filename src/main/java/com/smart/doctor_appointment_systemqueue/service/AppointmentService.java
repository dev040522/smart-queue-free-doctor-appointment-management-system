package com.smart.doctor_appointment_systemqueue.service;

import com.smart.doctor_appointment_systemqueue.exception.BadRequestException;
import com.smart.doctor_appointment_systemqueue.exception.ConflictException;
import com.smart.doctor_appointment_systemqueue.exception.ResourceNotFoundException;
import com.smart.doctor_appointment_systemqueue.model.Appointment;
import com.smart.doctor_appointment_systemqueue.model.Doctor;
import com.smart.doctor_appointment_systemqueue.repository.AppointmentRepository;
import com.smart.doctor_appointment_systemqueue.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AppointmentService {
    private static final DateTimeFormatter TWENTY_FOUR_HOUR = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TWELVE_HOUR = DateTimeFormatter.ofPattern("hh:mm a");
    private static final Set<String> TERMINAL_STATUSES = Set.of("Completed", "Cancelled");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Map<String, Integer> SPECIALTY_PREMIUMS = Map.of(
            "Cardiology", 220,
            "Orthopedics", 180,
            "Pediatrics", 140,
            "Neurology", 210,
            "Dermatology", 120,
            "ENT", 110,
            "Gynecology", 170,
            "General Medicine", 100
    );

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Appointment createAppointment(
            Long doctorId,
            String userName,
            String userEmail,
            String symptoms,
            String appointmentDate,
            String appointmentTime,
            String consultationType
    ) {
        if (doctorId == null) {
            throw new BadRequestException("Doctor is required");
        }

        if (userName == null || userName.isBlank()) {
            throw new BadRequestException("Patient name is required");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Selected doctor not found"));

        String normalizedName = userName.trim();
        if (normalizedName.length() < 2 || normalizedName.length() > 80) {
            throw new BadRequestException("Patient name must be between 2 and 80 characters");
        }

        String normalizedEmail = normalizeEmail(userEmail);
        String safeDate = resolveAppointmentDate(appointmentDate);
        String resolvedTime = resolveAppointmentTime(doctor, appointmentTime, safeDate);
        String normalizedConsultationType = normalizeConsultationType(consultationType);

        if (appointmentRepository.existsByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
                doctor.getDoctorName(),
                safeDate,
                resolvedTime,
                TERMINAL_STATUSES
        )) {
            throw new ConflictException("Selected time slot is already booked");
        }

        int token = appointmentRepository
                .findTopByDoctorNameAndAppointmentDateOrderByTokenNumberDesc(
                        doctor.getDoctorName(),
                        safeDate
                )
                .map(appointment -> appointment.getTokenNumber() + 1)
                .orElse(1);

        Appointment appointment = new Appointment();
        appointment.setUserName(normalizedName);
        appointment.setUserEmail(normalizedEmail);
        appointment.setSymptoms(normalizeSymptoms(symptoms));
        appointment.setSpecialization(doctor.getSpecialization());
        appointment.setDoctorName(doctor.getDoctorName());
        appointment.setAppointmentDate(safeDate);
        appointment.setAppointmentTime(resolvedTime);
        appointment.setConsultationType(normalizedConsultationType);
        appointment.setConsultationFee(calculateConsultationFee(doctor, normalizedConsultationType));
        appointment.setTokenNumber(token);
        appointment.setStatus(token == 1 ? "Now calling" : token == 2 ? "Up next" : "Waiting");

        Appointment saved = appointmentRepository.save(appointment);
        rebalanceDoctorQueue(saved.getDoctorName(), saved.getAppointmentDate(), null);
        return appointmentRepository.findById(saved.getId()).orElse(saved);
    }

    public List<Appointment> getTodayQueue() {
        return appointmentRepository.findAllByAppointmentDateOrderByAppointmentTimeAscTokenNumberAsc(
                LocalDate.now().toString()
        );
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAllByOrderByAppointmentDateDescAppointmentTimeAscTokenNumberAsc();
    }

    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        String doctorName = appointment.getDoctorName();
        String appointmentDate = appointment.getAppointmentDate();

        appointmentRepository.delete(appointment);

        if (doctorName != null && appointmentDate != null) {
            rebalanceDoctorQueue(doctorName, appointmentDate, null);
        }
    }

    public Appointment markAppointmentNowCalling(Long appointmentId) {
        return updateAppointmentStatus(appointmentId, "Now calling");
    }

    public List<Appointment> getAppointmentsForUser(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("User email is required");
        }

        return appointmentRepository.findAllByUserEmailOrderByAppointmentDateDescAppointmentTimeDescIdDesc(
                normalizeEmail(userEmail)
        );
    }

    public List<Appointment> getAppointmentsForDoctor(String doctorName, String appointmentDate) {
        if (doctorName == null || doctorName.isBlank()) {
            throw new BadRequestException("Doctor name is required");
        }

        String normalizedDoctorName = doctorName.trim();
        if (appointmentDate != null && !appointmentDate.isBlank()) {
            return appointmentRepository.findAllByDoctorNameAndAppointmentDateOrderByAppointmentTimeAscTokenNumberAsc(
                    normalizedDoctorName,
                    resolveAppointmentDate(appointmentDate)
            );
        }

        return appointmentRepository.findAllByDoctorNameOrderByAppointmentDateDescAppointmentTimeAscTokenNumberAsc(
                normalizedDoctorName
        );
    }

    @Transactional
    public Appointment updateAppointmentStatus(Long id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        appointment.setStatus(normalizeStatus(status));
        Appointment saved = appointmentRepository.save(appointment);
        rebalanceDoctorQueue(saved.getDoctorName(), saved.getAppointmentDate(), saved.getId());
        return appointmentRepository.findById(saved.getId()).orElse(saved);
    }

    private String resolveAppointmentTime(Doctor doctor, String appointmentTime, String appointmentDate) {
        LocalTime doctorStart = parseTwentyFourHour(doctor.getAvailableFrom(), "Doctor availability");
        LocalTime doctorEnd = parseTwentyFourHour(doctor.getAvailableTo(), "Doctor availability");

        if (appointmentTime != null && !appointmentTime.isBlank()) {
            LocalTime selectedTime = parseFlexibleTime(appointmentTime);
            validateWithinDoctorWindow(selectedTime, doctorStart, doctorEnd);
            return selectedTime.format(TWENTY_FOUR_HOUR);
        }

        int nextToken = appointmentRepository.countByDoctorNameAndAppointmentDate(
                doctor.getDoctorName(),
                appointmentDate
        );
        LocalTime generatedTime = doctorStart.plusMinutes(nextToken * 15L);
        if (!generatedTime.isBefore(doctorEnd)) {
            throw new ConflictException("Doctor has no more available slots for the selected date");
        }

        return generatedTime.format(TWENTY_FOUR_HOUR);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new BadRequestException("Status is required");
        }

        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "completed" -> "Completed";
            case "now calling", "calling", "in progress" -> "Now calling";
            case "up next", "next" -> "Up next";
            case "waiting" -> "Waiting";
            case "cancelled", "canceled" -> "Cancelled";
            default -> throw new BadRequestException("Unsupported appointment status");
        };
    }

    private String normalizeConsultationType(String consultationType) {
        if (consultationType == null || consultationType.isBlank()) {
            return "Hospital Visit";
        }

        return switch (consultationType.trim().toLowerCase(Locale.ROOT)) {
            case "hospital", "hospital visit", "clinic visit" -> "Hospital Visit";
            case "online", "online review", "video consultation" -> "Online Review";
            case "home", "home visit", "doorstep visit" -> "Home Visit";
            default -> throw new BadRequestException("Unsupported consultation type");
        };
    }

    private void rebalanceDoctorQueue(String doctorName, String appointmentDate, Long preferredActiveId) {
        List<Appointment> doctorQueue =
                appointmentRepository.findAllByDoctorNameAndAppointmentDateOrderByAppointmentTimeAscTokenNumberAsc(
                        doctorName,
                        appointmentDate
                );

        Long activeId = doctorQueue.stream()
                .filter(appointment -> "Now calling".equals(appointment.getStatus()))
                .map(Appointment::getId)
                .findFirst()
                .orElse(null);

        if (preferredActiveId != null) {
            Appointment preferred = doctorQueue.stream()
                    .filter(appointment -> appointment.getId().equals(preferredActiveId))
                    .findFirst()
                    .orElse(null);

            if (preferred != null && !TERMINAL_STATUSES.contains(preferred.getStatus())) {
                activeId = preferredActiveId;
            }
        }

        if (activeId == null) {
            activeId = doctorQueue.stream()
                    .filter(appointment -> !TERMINAL_STATUSES.contains(appointment.getStatus()))
                    .map(Appointment::getId)
                    .findFirst()
                    .orElse(null);
        }

        boolean encounteredActive = activeId == null;
        boolean upNextAssigned = false;

        for (Appointment queueEntry : doctorQueue) {
            if (TERMINAL_STATUSES.contains(queueEntry.getStatus())) {
                continue;
            }

            if (activeId != null && queueEntry.getId().equals(activeId)) {
                queueEntry.setStatus("Now calling");
                encounteredActive = true;
                continue;
            }

            if (!encounteredActive) {
                queueEntry.setStatus("Waiting");
                continue;
            }

            if (!upNextAssigned) {
                queueEntry.setStatus("Up next");
                upNextAssigned = true;
            } else {
                queueEntry.setStatus("Waiting");
            }
        }

        appointmentRepository.saveAll(doctorQueue);
    }

    private String resolveAppointmentDate(String appointmentDate) {
        LocalDate parsedDate;

        if (appointmentDate == null || appointmentDate.isBlank()) {
            parsedDate = LocalDate.now();
        } else {
            try {
                parsedDate = LocalDate.parse(appointmentDate.trim());
            } catch (DateTimeParseException exception) {
                throw new BadRequestException("Appointment date must be in yyyy-MM-dd format");
            }
        }

        if (parsedDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Appointment date cannot be in the past");
        }

        return parsedDate.toString();
    }

    private String normalizeEmail(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return null;
        }

        String normalizedEmail = userEmail.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new BadRequestException("Enter a valid email address");
        }

        return normalizedEmail;
    }

    private String normalizeSymptoms(String symptoms) {
        if (symptoms == null || symptoms.isBlank()) {
            return "";
        }

        String normalizedSymptoms = symptoms.trim();
        if (normalizedSymptoms.length() > 500) {
            throw new BadRequestException("Symptoms must be 500 characters or fewer");
        }

        return normalizedSymptoms;
    }

    private int calculateConsultationFee(Doctor doctor, String consultationType) {
        int yearsOfExperience = extractExperienceYears(doctor.getExperience());
        int hospitalFee = 320
                + SPECIALTY_PREMIUMS.getOrDefault(doctor.getSpecialization(), 130)
                + Math.min(yearsOfExperience * 12, 180);

        return switch (consultationType) {
            case "Home Visit" -> hospitalFee + 620;
            case "Online Review" -> hospitalFee + 240;
            default -> hospitalFee;
        };
    }

    private int extractExperienceYears(String experience) {
        if (experience == null || experience.isBlank()) {
            return 6;
        }

        String digits = experience.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return 6;
        }

        return Integer.parseInt(digits);
    }

    private LocalTime parseTwentyFourHour(String time, String fieldName) {
        try {
            return LocalTime.parse(time, TWENTY_FOUR_HOUR);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException(fieldName + " must be in HH:mm format");
        }
    }

    private LocalTime parseFlexibleTime(String appointmentTime) {
        try {
            return LocalTime.parse(appointmentTime.trim(), TWENTY_FOUR_HOUR);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalTime.parse(appointmentTime.trim().toUpperCase(Locale.ROOT), TWELVE_HOUR);
            } catch (DateTimeParseException exception) {
                throw new BadRequestException("Appointment time must be in HH:mm or hh:mm AM/PM format");
            }
        }
    }

    private void validateWithinDoctorWindow(
            LocalTime selectedTime,
            LocalTime availableFrom,
            LocalTime availableTo
    ) {
        if (selectedTime.isBefore(availableFrom) || !selectedTime.isBefore(availableTo)) {
            throw new BadRequestException("Selected time is outside doctor availability");
        }
    }
}
