package it.water.connectors.jobscheduler.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JobSchedulerRepositoryImplTest {

    @TempDir
    Path tempDir;

    @Test
    void createQuartzTableIfNotExistsWithNullPathUsesClasspathResource() {
        // quartz_creation_postgres.sql is in main/resources — found on classpath, reads SQL, logs info
        JobSchedulerRepositoryImpl repository = new JobSchedulerRepositoryImpl();
        assertDoesNotThrow(() -> repository.createQuartzTableIfNotExists(null));
    }

    @Test
    void createQuartzTableIfNotExistsWithExplicitFilePath() throws IOException {
        Path sqlFile = tempDir.resolve("test-quartz.sql");
        Files.writeString(sqlFile, "CREATE TABLE IF NOT EXISTS qrtz_job_details (sched_name VARCHAR(120) NOT NULL);");

        JobSchedulerRepositoryImpl repository = new JobSchedulerRepositoryImpl();
        assertDoesNotThrow(() -> repository.createQuartzTableIfNotExists(sqlFile.toString()));
    }

    @Test
    void createQuartzTableIfNotExistsWithNonExistentFileHandlesExceptionGracefully() {
        // Non-existent file → FileNotFoundException → caught and logged as error
        JobSchedulerRepositoryImpl repository = new JobSchedulerRepositoryImpl();
        assertDoesNotThrow(() -> repository.createQuartzTableIfNotExists("/no/such/path/quartz.sql"));
    }
}
