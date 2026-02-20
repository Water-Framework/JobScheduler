package it.water.connectors.jobscheduler;

import it.water.connectors.jobscheduler.api.JobSchedulerSystemApi;
import it.water.connectors.jobscheduler.service.JobSchedulerServiceImpl;
import it.water.core.api.registry.ComponentRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *  unit tests for JobScheduler service wiring.
 */
class JobSchedulerApiTest {

    @Test
    void systemServiceSetterGetterWorks() {
        JobSchedulerServiceImpl service = new JobSchedulerServiceImpl();
        JobSchedulerSystemApi systemApi = Mockito.mock(JobSchedulerSystemApi.class);

        service.setSystemService(systemApi);

        Assertions.assertSame(systemApi, service.getSystemService());
    }

    @Test
    void componentRegistrySetterGetterWorks() {
        JobSchedulerServiceImpl service = new JobSchedulerServiceImpl();
        ComponentRegistry componentRegistry = Mockito.mock(ComponentRegistry.class);

        service.setComponentRegistry(componentRegistry);

        Assertions.assertSame(componentRegistry, service.getComponentRegistry());
    }
}
