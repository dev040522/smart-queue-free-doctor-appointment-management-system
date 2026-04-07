package com.smart.doctor_appointment_systemqueue.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemaMigrationOrderConfig {

    @Bean
    static BeanFactoryPostProcessor entityManagerFactoryDependsOnAppointmentSchemaMigrator() {
        return beanFactory -> {
            if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
                beanFactory.getBeanDefinition("entityManagerFactory")
                        .setDependsOn("appointmentSchemaMigrator");
            }
        };
    }
}
