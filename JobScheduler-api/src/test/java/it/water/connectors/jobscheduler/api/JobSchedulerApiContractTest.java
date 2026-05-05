package it.water.connectors.jobscheduler.api;

import io.swagger.annotations.Api;
import it.water.connectors.jobscheduler.api.rest.JobSchedulerRestApi;
import it.water.core.api.service.BaseApi;
import it.water.core.api.service.BaseSystemApi;
import it.water.core.api.service.rest.FrameworkRestApi;
import it.water.core.api.service.rest.RestApi;
import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.quartz.JobKey;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JobSchedulerApiContractTest {

    @Test
    void jobSchedulerApiExtendsBaseApi() {
        assertTrue(JobSchedulerApi.class.isInterface());
        assertTrue(BaseApi.class.isAssignableFrom(JobSchedulerApi.class));
        assertEquals(0, JobSchedulerApi.class.getDeclaredMethods().length);
    }

    @Test
    void jobSchedulerSystemApiDeclaresExpectedMethods() throws NoSuchMethodException {
        assertTrue(JobSchedulerSystemApi.class.isInterface());
        assertTrue(BaseSystemApi.class.isAssignableFrom(JobSchedulerSystemApi.class));
        assertMethod(JobSchedulerSystemApi.class, "addJob", void.class, WaterJob.class);
        assertMethod(JobSchedulerSystemApi.class, "deleteJob", void.class, WaterJob.class);
        assertMethod(JobSchedulerSystemApi.class, "updateJob", void.class, WaterJob.class);
        assertEquals(3, JobSchedulerSystemApi.class.getDeclaredMethods().length);
    }

    @Test
    void waterJobDeclaresExpectedContract() throws NoSuchMethodException {
        assertTrue(WaterJob.class.isInterface());
        assertMethod(WaterJob.class, "getClassName", String.class);
        assertMethod(WaterJob.class, "getCronExpression", String.class);
        assertMethod(WaterJob.class, "getJobDetail", JobDetail.class);
        assertMethod(WaterJob.class, "getJobKey", JobKey.class);
        assertMethod(WaterJob.class, "getJobParams", Map.class);
        assertMethod(WaterJob.class, "isActive", boolean.class);
        assertEquals(6, WaterJob.class.getDeclaredMethods().length);
    }

    @Test
    void jobSchedulerRepositoryExposesQuartzInitMethod() throws NoSuchMethodException {
        assertTrue(JobSchedulerRepository.class.isInterface());
        assertMethod(JobSchedulerRepository.class, "createQuartzTableIfNotExists", void.class, String.class);
        assertEquals(1, JobSchedulerRepository.class.getDeclaredMethods().length);
    }

    @Test
    void jobSchedulerRestApiHasExpectedAnnotations() {
        assertTrue(JobSchedulerRestApi.class.isInterface());
        assertTrue(RestApi.class.isAssignableFrom(JobSchedulerRestApi.class));
        assertTrue(JobSchedulerRestApi.class.isAnnotationPresent(FrameworkRestApi.class));

        Path path = JobSchedulerRestApi.class.getAnnotation(Path.class);
        assertNotNull(path);
        assertEquals("/jobSchedulers", path.value());

        Api api = JobSchedulerRestApi.class.getAnnotation(Api.class);
        assertNotNull(api);
        assertEquals(MediaType.APPLICATION_JSON, api.produces());
        assertArrayEquals(new String[]{"JobScheduler API"}, api.tags());
    }

    private void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = type.getDeclaredMethod(methodName, parameterTypes);
        assertEquals(returnType, method.getReturnType());
    }
}
