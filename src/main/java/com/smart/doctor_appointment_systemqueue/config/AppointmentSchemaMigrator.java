package com.smart.doctor_appointment_systemqueue.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class AppointmentSchemaMigrator {
    private static final Logger log = LoggerFactory.getLogger(AppointmentSchemaMigrator.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public AppointmentSchemaMigrator(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void migrateOnStartup() {
        ensureAppointmentColumns();
    }

    void ensureAppointmentColumns() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            if (!tableExists(metaData, "appointment")) {
                return;
            }

            addColumnIfMissing(
                    metaData,
                    "appointment",
                    "consultation_type",
                    "ALTER TABLE appointment ADD COLUMN consultation_type VARCHAR(255) DEFAULT 'Hospital Visit' NOT NULL"
            );
            addColumnIfMissing(
                    metaData,
                    "appointment",
                    "consultation_fee",
                    "ALTER TABLE appointment ADD COLUMN consultation_fee INT DEFAULT 0 NOT NULL"
            );
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to migrate appointment schema", exception);
        }
    }

    private void addColumnIfMissing(
            DatabaseMetaData metaData,
            String tableName,
            String columnName,
            String statement
    ) throws SQLException {
        if (columnExists(metaData, tableName, columnName)) {
            return;
        }

        log.info("Adding missing {}.{} column", tableName, columnName);
        jdbcTemplate.execute(statement);
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"})) {
            while (tables.next()) {
                String existingTable = tables.getString("TABLE_NAME");
                if (existingTable != null && existingTable.equalsIgnoreCase(tableName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean columnExists(DatabaseMetaData metaData, String tableName, String columnName) throws SQLException {
        try (ResultSet columns = metaData.getColumns(null, null, null, null)) {
            while (columns.next()) {
                String existingTable = columns.getString("TABLE_NAME");
                String existingColumn = columns.getString("COLUMN_NAME");
                if (existingTable != null
                        && existingColumn != null
                        && existingTable.equalsIgnoreCase(tableName)
                        && existingColumn.equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
        }

        return false;
    }
}
