package com.smart.doctor_appointment_systemqueue.service;

import com.smart.doctor_appointment_systemqueue.dto.AccountProfile;
import com.smart.doctor_appointment_systemqueue.exception.ConflictException;
import com.smart.doctor_appointment_systemqueue.model.DoctorAccount;
import com.smart.doctor_appointment_systemqueue.model.PatientAccount;
import com.smart.doctor_appointment_systemqueue.model.User;
import com.smart.doctor_appointment_systemqueue.repository.DoctorAccountRepository;
import com.smart.doctor_appointment_systemqueue.repository.PatientAccountRepository;
import com.smart.doctor_appointment_systemqueue.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientAccountRepository patientAccountRepository;

    @Autowired
    private DoctorAccountRepository doctorAccountRepository;

    @Autowired
    private PasswordService passwordService;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
        patientAccountRepository.deleteAll();
        doctorAccountRepository.deleteAll();
    }

    @Test
    void registerUserHashesPasswordAndNormalizesFields() {
        User user = new User();
        user.setName("  Dev Singh  ");
        user.setEmail("  DEV@Example.com ");
        user.setPassword("strongpass123");
        user.setPhoneNumber("9911400383");
        user.setRole("Patient");

        AccountProfile savedUser = userService.registerUser(user);
        PatientAccount storedPatient = patientAccountRepository.findByEmail("dev@example.com").orElseThrow();

        assertEquals("Dev Singh", savedUser.getName());
        assertEquals("dev@example.com", savedUser.getEmail());
        assertEquals("patient", savedUser.getRole());
        assertEquals("patient", savedUser.getAccountType());
        assertTrue(savedUser.getUserId().startsWith("patient-"));
        assertNotEquals("strongpass123", storedPatient.getPassword());
        assertTrue(passwordService.matches("strongpass123", storedPatient.getPassword()));
    }

    @Test
    void loginUserUpgradesLegacyPlainTextPassword() {
        User legacyUser = new User();
        legacyUser.setName("Legacy User");
        legacyUser.setEmail("legacy@example.com");
        legacyUser.setPassword("legacypass123");
        legacyUser.setRole("admin");
        legacyUser = userRepository.save(legacyUser);

        Optional<AccountProfile> loggedInUser = userService.loginUser("legacy@example.com", "legacypass123");

        assertTrue(loggedInUser.isPresent());
        assertEquals("admin", loggedInUser.orElseThrow().getRole());

        User refreshedUser = userRepository.findById(legacyUser.getUserId()).orElseThrow();
        assertTrue(passwordService.isHashed(refreshedUser.getPassword()));
        assertTrue(passwordService.matches("legacypass123", refreshedUser.getPassword()));
    }

    @Test
    void registerUserRejectsDuplicateEmail() {
        User firstUser = new User();
        firstUser.setName("First User");
        firstUser.setEmail("duplicate@example.com");
        firstUser.setPassword("duplicate123");
        firstUser.setRole("patient");
        userService.registerUser(firstUser);

        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("DUPLICATE@example.com");
        secondUser.setPassword("duplicate456");
        secondUser.setRole("doctor");

        assertThrows(ConflictException.class, () -> userService.registerUser(secondUser));
    }

    @Test
    void registerDoctorStoresAccountInSeparateDoctorTable() {
        User doctor = new User();
        doctor.setName("Dr. Maya Nair");
        doctor.setEmail("maya.nair@example.com");
        doctor.setPassword("doctorpass123");
        doctor.setPhoneNumber("9911400384");
        doctor.setRole("doctor");

        AccountProfile savedDoctor = userService.registerUser(doctor);
        DoctorAccount storedDoctor = doctorAccountRepository.findByEmail("maya.nair@example.com")
                .orElseThrow();

        assertEquals("doctor", savedDoctor.getRole());
        assertEquals("doctor", savedDoctor.getAccountType());
        assertTrue(savedDoctor.getUserId().startsWith("doctor-"));
        assertTrue(passwordService.matches("doctorpass123", storedDoctor.getPassword()));
    }

    @Test
    void adminCanViewCountsAndUpdateRole() {
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setPassword("adminpass123");
        admin.setRole("admin");
        AccountProfile savedAdmin = userService.registerUser(admin);

        assertEquals(1, userService.countAdmins());
        assertEquals(0, userService.countDoctors());
        assertEquals(0, userService.countPatients());

        AccountProfile updated = userService.updateAdminRole(Long.parseLong(savedAdmin.getUserId().split("-")[1]), "admin");
        assertEquals("admin", updated.getRole());
    }

    @Test
    void updateUserEditsPatientAccountDetails() {
        User patient = new User();
        patient.setName("Patient User");
        patient.setEmail("patient.user@example.com");
        patient.setPassword("patientpass123");
        patient.setPhoneNumber("9911400385");
        patient.setRole("patient");

        AccountProfile savedPatient = userService.registerUser(patient);

        AccountProfile updatedPatient = userService.updateUser(
                savedPatient.getUserId(),
                "Updated Patient",
                "updated.patient@example.com",
                "9911400399"
        );

        assertEquals("Updated Patient", updatedPatient.getName());
        assertEquals("updated.patient@example.com", updatedPatient.getEmail());
        assertEquals("9911400399", updatedPatient.getPhoneNumber());
        assertTrue(patientAccountRepository.findByEmail("updated.patient@example.com").isPresent());
    }
}
