package com.smart.doctor_appointment_systemqueue.config;

import com.smart.doctor_appointment_systemqueue.model.DoctorAccount;
import com.smart.doctor_appointment_systemqueue.model.PatientAccount;
import com.smart.doctor_appointment_systemqueue.model.User;
import com.smart.doctor_appointment_systemqueue.repository.DoctorAccountRepository;
import com.smart.doctor_appointment_systemqueue.repository.PatientAccountRepository;
import com.smart.doctor_appointment_systemqueue.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Locale;

@Configuration
public class AccountMigrationConfig {

    @Bean
    CommandLineRunner migrateLegacyAccounts(
            UserRepository userRepository,
            PatientAccountRepository patientAccountRepository,
            DoctorAccountRepository doctorAccountRepository
    ) {
        return args -> {
            List<User> legacyUsers = userRepository.findAll();

            for (User legacyUser : legacyUsers) {
                if (legacyUser.getRole() == null) {
                    continue;
                }

                String normalizedRole = legacyUser.getRole().trim().toLowerCase(Locale.ROOT);
                switch (normalizedRole) {
                    case "patient" -> migratePatient(legacyUser, userRepository, patientAccountRepository);
                    case "doctor" -> migrateDoctor(legacyUser, userRepository, doctorAccountRepository);
                    default -> {
                    }
                }
            }
        };
    }

    private void migratePatient(
            User legacyUser,
            UserRepository userRepository,
            PatientAccountRepository patientAccountRepository
    ) {
        if (patientAccountRepository.findByEmail(legacyUser.getEmail()).isEmpty()) {
            PatientAccount patientAccount = new PatientAccount();
            patientAccount.setName(legacyUser.getName());
            patientAccount.setEmail(legacyUser.getEmail());
            patientAccount.setPassword(legacyUser.getPassword());
            patientAccount.setPhoneNumber(legacyUser.getPhoneNumber());
            patientAccountRepository.save(patientAccount);
        }

        userRepository.delete(legacyUser);
    }

    private void migrateDoctor(
            User legacyUser,
            UserRepository userRepository,
            DoctorAccountRepository doctorAccountRepository
    ) {
        if (doctorAccountRepository.findByEmail(legacyUser.getEmail()).isEmpty()) {
            DoctorAccount doctorAccount = new DoctorAccount();
            doctorAccount.setName(legacyUser.getName());
            doctorAccount.setEmail(legacyUser.getEmail());
            doctorAccount.setPassword(legacyUser.getPassword());
            doctorAccount.setPhoneNumber(legacyUser.getPhoneNumber());
            doctorAccountRepository.save(doctorAccount);
        }

        userRepository.delete(legacyUser);
    }
}
