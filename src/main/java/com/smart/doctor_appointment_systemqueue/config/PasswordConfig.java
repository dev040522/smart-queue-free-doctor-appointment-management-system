package com.smart.doctor_appointment_systemqueue.config;

import com.smart.doctor_appointment_systemqueue.service.PasswordService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordConfig {

    @Bean
    PasswordService passwordService() {
        return new PasswordService();
    }
}
