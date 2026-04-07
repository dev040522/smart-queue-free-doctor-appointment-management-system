package com.smart.doctor_appointment_systemqueue.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AppointmentSchemaMigratorTests {

    @Autowired
    private AppointmentSchemaMigrator appointmentSchemaMigrator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void ensureAppointmentColumnsRestoresMissingConsultationFields() {
        jdbcTemplate.execute("ALTER TABLE appointment DROP COLUMN consultation_fee");
        jdbcTemplate.execute("ALTER TABLE appointment DROP COLUMN consultation_type");

        appointmentSchemaMigrator.ensureAppointmentColumns();

        Integer typeColumnCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'APPOINTMENT'
                  AND COLUMN_NAME = 'CONSULTATION_TYPE'
                """,
                Integer.class
        );
        Integer feeColumnCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'APPOINTMENT'
                  AND COLUMN_NAME = 'CONSULTATION_FEE'
                """,
                Integer.class
        );

        assertEquals(1, typeColumnCount);
        assertEquals(1, feeColumnCount);
    }
}
