package com.smart.doctor_appointment_systemqueue.service;


import com.smart.doctor_appointment_systemqueue.dto.AccountProfile;
import com.smart.doctor_appointment_systemqueue.exception.BadRequestException;
import com.smart.doctor_appointment_systemqueue.exception.ConflictException;
import com.smart.doctor_appointment_systemqueue.exception.ResourceNotFoundException;
import com.smart.doctor_appointment_systemqueue.model.DoctorAccount;
import com.smart.doctor_appointment_systemqueue.model.PatientAccount;
import com.smart.doctor_appointment_systemqueue.model.User;
import com.smart.doctor_appointment_systemqueue.repository.DoctorAccountRepository;
import com.smart.doctor_appointment_systemqueue.repository.PatientAccountRepository;
import com.smart.doctor_appointment_systemqueue.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+()\\-\\s]{7,20}$");
    private static final Set<String> ALLOWED_ROLES = Set.of("patient", "doctor", "admin");

    private final UserRepository userRepository;
    private final PatientAccountRepository patientAccountRepository;
    private final DoctorAccountRepository doctorAccountRepository;
    private final PasswordService passwordService;

    public UserService(
            UserRepository userRepository,
            PatientAccountRepository patientAccountRepository,
            DoctorAccountRepository doctorAccountRepository,
            PasswordService passwordService
    ) {
        this.userRepository = userRepository;
        this.patientAccountRepository = patientAccountRepository;
        this.doctorAccountRepository = doctorAccountRepository;
        this.passwordService = passwordService;
    }

    public AccountProfile registerUser(User user) {
        validateUserRegistration(user);

        String normalizedEmail = normalizeEmail(user.getEmail());
        String normalizedRole = user.getRole().trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = normalizePhoneNumber(user.getPhoneNumber());

        if (emailExistsAcrossAccounts(normalizedEmail)) {
            throw new ConflictException("Email already exists");
        }

        return switch (normalizedRole) {
            case "patient" -> registerPatientAccount(user, normalizedEmail, normalizedPhone);
            case "doctor" -> registerDoctorAccount(user, normalizedEmail, normalizedPhone);
            default -> registerAdminAccount(user, normalizedEmail, normalizedPhone);
        };
    }

    public List<AccountProfile> getAllUsers() {
        List<AccountProfile> profiles = new ArrayList<>();
        patientAccountRepository.findAllByOrderByNameAscPatientIdAsc()
                .forEach(account -> profiles.add(toProfile(account)));
        doctorAccountRepository.findAllByOrderByNameAscDoctorAccountIdAsc()
                .forEach(account -> profiles.add(toProfile(account)));
        userRepository.findAllByOrderByRoleAscNameAscUserIdAsc()
                .forEach(account -> profiles.add(toProfile(account)));

        profiles.sort(Comparator
                .comparing(AccountProfile::getRole)
                .thenComparing(AccountProfile::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(AccountProfile::getRecordId));
        return profiles;
    }

    public long countPatients() {
        return patientAccountRepository.count();
    }

    public long countDoctors() {
        return doctorAccountRepository.count();
    }

    public long countAdmins() {
        return userRepository.count();
    }

    public AccountProfile updateAdminRole(Long adminUserId, String newRole) {
        if (newRole == null || newRole.isBlank()) {
            throw new BadRequestException("Role is required");
        }

        String normalizedRole = newRole.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(normalizedRole)) {
            throw new BadRequestException("Role must be patient, doctor, or admin");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin account not found"));

        if (!"admin".equalsIgnoreCase(admin.getRole())) {
            throw new BadRequestException("Can only update role for admin accounts");
        }

        if (!"admin".equals(normalizedRole)) {
            throw new BadRequestException("Only preserving admin role is supported in this flow");
        }

        admin.setRole("admin");
        return toProfile(userRepository.save(admin));
    }

    public AccountProfile getUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("User id is required");
        }

        AccountKey accountKey = parseAccountKey(userId);

        return switch (accountKey.accountType()) {
            case "patient" -> toProfile(
                    patientAccountRepository.findById(accountKey.recordId())
                            .orElseThrow(() -> new ResourceNotFoundException("Patient account not found"))
            );
            case "doctor" -> toProfile(
                    doctorAccountRepository.findById(accountKey.recordId())
                            .orElseThrow(() -> new ResourceNotFoundException("Doctor account not found"))
            );
            case "admin" -> toProfile(
                    userRepository.findById(accountKey.recordId())
                            .orElseThrow(() -> new ResourceNotFoundException("Admin account not found"))
            );
            default -> throw new ResourceNotFoundException("User not found");
        };
    }

    public AccountProfile updateUser(String userId, String name, String email, String phoneNumber) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("User id is required");
        }

        AccountKey accountKey = parseAccountKey(userId);

        return switch (accountKey.accountType()) {
            case "patient" -> updatePatientAccount(accountKey, name, email, phoneNumber);
            case "doctor" -> updateDoctorAccount(accountKey, name, email, phoneNumber);
            case "admin" -> updateAdminAccount(accountKey, name, email, phoneNumber);
            default -> throw new ResourceNotFoundException("User not found");
        };
    }

    public void deleteUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("User id is required");
        }

        AccountKey accountKey = parseAccountKey(userId);

        switch (accountKey.accountType()) {
            case "patient" -> patientAccountRepository.delete(
                    patientAccountRepository.findById(accountKey.recordId())
                            .orElseThrow(() -> new ResourceNotFoundException("Patient account not found"))
            );
            case "doctor" -> doctorAccountRepository.delete(
                    doctorAccountRepository.findById(accountKey.recordId())
                            .orElseThrow(() -> new ResourceNotFoundException("Doctor account not found"))
            );
            case "admin" -> userRepository.delete(
                    userRepository.findById(accountKey.recordId())
                            .orElseThrow(() -> new ResourceNotFoundException("Admin account not found"))
            );
            default -> throw new ResourceNotFoundException("User not found");
        }
    }

    public Optional<AccountProfile> loginUser(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        String normalizedEmail = normalizeEmail(email);

        Optional<AccountProfile> doctorLogin = doctorAccountRepository.findByEmail(normalizedEmail)
                .filter(account -> passwordService.matches(password, account.getPassword()))
                .map(account -> {
                    upgradeDoctorPasswordIfNeeded(account, password);
                    return toProfile(account);
                });

        if (doctorLogin.isPresent()) {
            return doctorLogin;
        }

        Optional<AccountProfile> patientLogin = patientAccountRepository.findByEmail(normalizedEmail)
                .filter(account -> passwordService.matches(password, account.getPassword()))
                .map(account -> {
                    upgradePatientPasswordIfNeeded(account, password);
                    return toProfile(account);
                });

        if (patientLogin.isPresent()) {
            return patientLogin;
        }

        return userRepository.findByEmail(normalizedEmail)
                .filter(user -> passwordService.matches(password, user.getPassword()))
                .map(user -> {
                    upgradeLegacyPasswordIfNeeded(user, password);
                    return toProfile(user);
                });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AccountProfile updatePatientAccount(
            AccountKey accountKey,
            String name,
            String email,
            String phoneNumber
    ) {
        PatientAccount account = patientAccountRepository.findById(accountKey.recordId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient account not found"));

        account.setName(resolveUpdatedName(name, account.getName()));
        account.setEmail(resolveUpdatedEmail(accountKey, email, account.getEmail()));
        account.setPhoneNumber(resolveUpdatedPhoneNumber(phoneNumber, account.getPhoneNumber()));
        return toProfile(patientAccountRepository.save(account));
    }

    private AccountProfile updateDoctorAccount(
            AccountKey accountKey,
            String name,
            String email,
            String phoneNumber
    ) {
        DoctorAccount account = doctorAccountRepository.findById(accountKey.recordId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor account not found"));

        account.setName(resolveUpdatedName(name, account.getName()));
        account.setEmail(resolveUpdatedEmail(accountKey, email, account.getEmail()));
        account.setPhoneNumber(resolveUpdatedPhoneNumber(phoneNumber, account.getPhoneNumber()));
        return toProfile(doctorAccountRepository.save(account));
    }

    private AccountProfile updateAdminAccount(
            AccountKey accountKey,
            String name,
            String email,
            String phoneNumber
    ) {
        User account = userRepository.findById(accountKey.recordId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin account not found"));

        account.setName(resolveUpdatedName(name, account.getName()));
        account.setEmail(resolveUpdatedEmail(accountKey, email, account.getEmail()));
        account.setPhoneNumber(resolveUpdatedPhoneNumber(phoneNumber, account.getPhoneNumber()));
        return toProfile(userRepository.save(account));
    }

    private String resolveUpdatedName(String requestedName, String currentName) {
        if (requestedName == null) {
            return currentName;
        }

        if (requestedName.isBlank()) {
            throw new BadRequestException("Name is required");
        }

        String trimmedName = requestedName.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 80) {
            throw new BadRequestException("Name must be between 2 and 80 characters");
        }

        return trimmedName;
    }

    private String resolveUpdatedEmail(AccountKey accountKey, String requestedEmail, String currentEmail) {
        if (requestedEmail == null) {
            return currentEmail;
        }

        if (requestedEmail.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        String normalizedEmail = normalizeEmail(requestedEmail);
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new BadRequestException("Enter a valid email address");
        }

        if (!normalizedEmail.equals(currentEmail) && emailExistsAcrossAccountsExcluding(accountKey, normalizedEmail)) {
            throw new ConflictException("Email already exists");
        }

        return normalizedEmail;
    }

    private String resolveUpdatedPhoneNumber(String requestedPhoneNumber, String currentPhoneNumber) {
        if (requestedPhoneNumber == null) {
            return currentPhoneNumber;
        }

        if (requestedPhoneNumber.isBlank()) {
            return null;
        }

        return normalizePhoneNumber(requestedPhoneNumber);
    }

    private void validateUserRegistration(User user) {
        if (user == null) {
            throw new BadRequestException("User details are required");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new BadRequestException("Name is required");
        }

        String trimmedName = user.getName().trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 80) {
            throw new BadRequestException("Name must be between 2 and 80 characters");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        String normalizedEmail = normalizeEmail(user.getEmail());
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new BadRequestException("Enter a valid email address");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        if (user.getPassword().trim().length() < 8 || user.getPassword().length() > 120) {
            throw new BadRequestException("Password must be between 8 and 120 characters");
        }

        if (user.getRole() == null || user.getRole().isBlank()) {
            throw new BadRequestException("Role is required");
        }

        String normalizedRole = user.getRole().trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(normalizedRole)) {
            throw new BadRequestException("Role must be patient, doctor, or admin");
        }

        normalizePhoneNumber(user.getPhoneNumber());
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        String trimmedPhoneNumber = phoneNumber.trim();
        if (!PHONE_PATTERN.matcher(trimmedPhoneNumber).matches()) {
            throw new BadRequestException("Enter a valid phone number");
        }

        return trimmedPhoneNumber;
    }

    private void upgradeLegacyPasswordIfNeeded(User user, String rawPassword) {
        if (passwordService.isHashed(user.getPassword())) {
            return;
        }

        if (rawPassword.equals(user.getPassword())) {
            user.setPassword(passwordService.hashPassword(rawPassword));
            userRepository.save(user);
        }
    }

    private AccountProfile registerPatientAccount(User user, String normalizedEmail, String normalizedPhone) {
        PatientAccount account = new PatientAccount();
        account.setName(user.getName().trim());
        account.setEmail(normalizedEmail);
        account.setPhoneNumber(normalizedPhone);
        account.setPassword(passwordService.hashPassword(user.getPassword()));
        return toProfile(patientAccountRepository.save(account));
    }

    private AccountProfile registerDoctorAccount(User user, String normalizedEmail, String normalizedPhone) {
        DoctorAccount account = new DoctorAccount();
        account.setName(user.getName().trim());
        account.setEmail(normalizedEmail);
        account.setPhoneNumber(normalizedPhone);
        account.setPassword(passwordService.hashPassword(user.getPassword()));
        return toProfile(doctorAccountRepository.save(account));
    }

    private AccountProfile registerAdminAccount(User user, String normalizedEmail, String normalizedPhone) {
        user.setEmail(normalizedEmail);
        user.setName(user.getName().trim());
        user.setRole("admin");
        user.setPhoneNumber(normalizedPhone);
        user.setPassword(passwordService.hashPassword(user.getPassword()));
        return toProfile(userRepository.save(user));
    }

    private boolean emailExistsAcrossAccounts(String normalizedEmail) {
        return userRepository.findByEmail(normalizedEmail).isPresent()
                || patientAccountRepository.findByEmail(normalizedEmail).isPresent()
                || doctorAccountRepository.findByEmail(normalizedEmail).isPresent();
    }

    private boolean emailExistsAcrossAccountsExcluding(AccountKey accountKey, String normalizedEmail) {
        Optional<User> adminMatch = userRepository.findByEmail(normalizedEmail)
                .filter(account -> !("admin".equals(accountKey.accountType())
                        && account.getUserId().equals(accountKey.recordId())));

        if (adminMatch.isPresent()) {
            return true;
        }

        Optional<PatientAccount> patientMatch = patientAccountRepository.findByEmail(normalizedEmail)
                .filter(account -> !("patient".equals(accountKey.accountType())
                        && account.getPatientId().equals(accountKey.recordId())));

        if (patientMatch.isPresent()) {
            return true;
        }

        return doctorAccountRepository.findByEmail(normalizedEmail)
                .filter(account -> !("doctor".equals(accountKey.accountType())
                        && account.getDoctorAccountId().equals(accountKey.recordId())))
                .isPresent();
    }

    private AccountProfile toProfile(PatientAccount account) {
        AccountProfile profile = new AccountProfile();
        profile.setUserId(createAccountKey("patient", account.getPatientId()));
        profile.setRecordId(account.getPatientId());
        profile.setAccountType("patient");
        profile.setName(account.getName());
        profile.setEmail(account.getEmail());
        profile.setPhoneNumber(account.getPhoneNumber());
        profile.setRole("patient");
        return profile;
    }

    private AccountProfile toProfile(DoctorAccount account) {
        AccountProfile profile = new AccountProfile();
        profile.setUserId(createAccountKey("doctor", account.getDoctorAccountId()));
        profile.setRecordId(account.getDoctorAccountId());
        profile.setAccountType("doctor");
        profile.setName(account.getName());
        profile.setEmail(account.getEmail());
        profile.setPhoneNumber(account.getPhoneNumber());
        profile.setRole("doctor");
        return profile;
    }

    private AccountProfile toProfile(User account) {
        AccountProfile profile = new AccountProfile();
        profile.setUserId(createAccountKey("admin", account.getUserId()));
        profile.setRecordId(account.getUserId());
        profile.setAccountType("admin");
        profile.setName(account.getName());
        profile.setEmail(account.getEmail());
        profile.setPhoneNumber(account.getPhoneNumber());
        profile.setRole(account.getRole());
        return profile;
    }

    private String createAccountKey(String accountType, Long recordId) {
        return accountType + "-" + recordId;
    }

    private AccountKey parseAccountKey(String accountKey) {
        String[] parts = accountKey.split("-", 2);
        if (parts.length != 2) {
            throw new BadRequestException("Invalid user id");
        }

        try {
            return new AccountKey(parts[0], Long.parseLong(parts[1]));
        } catch (NumberFormatException exception) {
            throw new BadRequestException("Invalid user id");
        }
    }

    private void upgradePatientPasswordIfNeeded(PatientAccount account, String rawPassword) {
        if (passwordService.isHashed(account.getPassword())) {
            return;
        }

        if (rawPassword.equals(account.getPassword())) {
            account.setPassword(passwordService.hashPassword(rawPassword));
            patientAccountRepository.save(account);
        }
    }

    private void upgradeDoctorPasswordIfNeeded(DoctorAccount account, String rawPassword) {
        if (passwordService.isHashed(account.getPassword())) {
            return;
        }

        if (rawPassword.equals(account.getPassword())) {
            account.setPassword(passwordService.hashPassword(rawPassword));
            doctorAccountRepository.save(account);
        }
    }

    private record AccountKey(String accountType, Long recordId) {
    }
}
