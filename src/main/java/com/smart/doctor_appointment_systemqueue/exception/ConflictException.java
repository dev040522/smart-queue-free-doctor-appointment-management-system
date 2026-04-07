package com.smart.doctor_appointment_systemqueue.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
