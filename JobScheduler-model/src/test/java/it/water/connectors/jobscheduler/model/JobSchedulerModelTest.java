package it.water.connectors.jobscheduler.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class JobSchedulerModelTest {

    @Test
    void jobSchedulerCanBeInstantiated() {
        JobScheduler jobScheduler = new JobScheduler();
        assertNotNull(jobScheduler);
        assertEquals(JobScheduler.class, jobScheduler.getClass());
    }

    @Test
    void jobSchedulerConstantsExposeExpectedValues() {
        assertEquals("it.water.connectors.jobscheduler", JobSchedulerConstants.JOB_SCHEDULER_CONFIG_PID);
        assertEquals("it.water.connectors.jobscheduler.init.script", JobSchedulerConstants.JOB_SCHEDULER_INIT_SCRIPT);
        assertEquals("org.quartz", JobSchedulerConstants.QUARTZ_PROPERTY_PREFIX);
    }

    @Test
    void jobSchedulerConstantsPrivateConstructorCanBeInvoked() throws ReflectiveOperationException {
        Constructor<JobSchedulerConstants> constructor = JobSchedulerConstants.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        assertTrue(Modifier.isFinal(JobSchedulerConstants.class.getModifiers()));
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}
