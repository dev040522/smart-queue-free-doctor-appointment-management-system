package com.smart.doctor_appointment_systemqueue.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctor_accounts")
public class DoctorAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorAccountId;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String phoneNumber;

    public Long getDoctorAccountId() {
        return doctorAccountId;
    }

    public void setDoctorAccountId(Long doctorAccountId) {
        this.doctorAccountId = doctorAccountId;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
