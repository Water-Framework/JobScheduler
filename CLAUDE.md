# JobScheduler Module — Distributed Quartz Job Scheduling

## Purpose
Provides cluster-aware Quartz 2.3.2 job scheduling for Water Framework services. Jobs are scheduled via cron expressions and run only on the cluster leader node (determined via ZooKeeper/Apache Curator leader election). Does NOT manage JPA entities — `WaterJob` is an interface, not a persisted entity. Quartz tables are managed via database initialization scripts.

## Sub-modules

| Sub-module | Runtime | Key Classes |
|---|---|---|
| `JobScheduler-api` | All | `JobSchedulerApi`, `JobSchedulerSystemApi`, `WaterJob`, `JobSchedulerRepository` |
| `JobScheduler-model` | All | `JobSchedulerConstants` |
| `JobScheduler-service` | Water/OSGi | `JobSchedulerSystemServiceImpl`, `JobSchedulerServiceImpl`, `JobSchedulerRestControllerImpl`, `JobSchedulerRepositoryImpl` |

## WaterJob Interface

```java
public interface WaterJob {
    String getClassName();               // Fully-qualified Quartz Job implementation class
    String getCronExpression();          // Cron expression (e.g., "0 0/5 * * * ?")
    JobDetail getJobDetail();            // Quartz JobDetail object
    JobKey getJobKey();                  // Quartz JobKey (name + group)
    Map<String, Object> getJobParams();  // Parameters passed to the job at execution
    boolean isActive();                  // Whether to schedule this job
}
```

## Key Operations

### JobSchedulerSystemApi (bypasses permissions)
```java
public interface JobSchedulerSystemApi extends BaseSystemApi {
    void addJob(WaterJob job);       // Schedule a new job (throws if invalid cron)
    void deleteJob(WaterJob job);    // Remove job from Quartz scheduler
    void updateJob(WaterJob job);    // Reschedule job with new cron expression
}
```

### JobSchedulerApi (permission-checked)
```java
// Extends BaseApi — permission system integrated
// Delegates to JobSchedulerSystemApi
```

### JobSchedulerRepository
```java
public interface JobSchedulerRepository {
    void createQuartzTableIfNotExists(String initScriptFilePath);
    // Executes Quartz DB init script if tables don't exist
}
```

## JobSchedulerSystemServiceImpl — OSGi Lifecycle

```java
@FrameworkComponent
public class JobSchedulerSystemServiceImpl implements JobSchedulerSystemApi {

    private Scheduler scheduler;            // Quartz scheduler instance
    private LeaderLatchListener leaderLatch; // ZooKeeper leader election

    @OnActivate
    public void activate() {
        // Load Quartz properties from classpath + ApplicationProperties
        // Create Quartz Scheduler (JDBC or in-memory store)
        // Register LeaderLatchListener for cluster leadership transitions
        // Start scheduler only if this node is the cluster leader
    }

    @OnDeactivate
    public void deactivate() {
        // Shutdown Quartz scheduler (waits for running jobs to complete)
        // Release ZooKeeper leader latch
    }

    // Called when this node becomes cluster leader
    private void onLeaderElected() {
        scheduler.start();  // Start scheduling jobs
    }

    // Called when this node loses leadership
    private void onLeaderRevoked() {
        scheduler.standby(); // Pause job execution
    }
}
```

## Cluster Behavior

```
Cluster node 1 (Leader)          Cluster node 2 (Standby)
  └─ Quartz scheduler: RUNNING     └─ Quartz scheduler: STANDBY
      └─ Jobs execute here             └─ Jobs NOT executed here

On leader failover:
  Node 2 wins ZK election → scheduler.start() → jobs resume on node 2
```

## REST Layer

`JobSchedulerRestApi` / `JobSchedulerRestControllerImpl` are currently placeholders only.
No concrete JAX-RS methods are exposed yet, so do not maintain fake CRUD docs, fake Bruno requests,
or empty Karate features for this module.

## Configuration

Configuration file: `etc/it.water.connectors.jobscheduler.cfg` (Karaf) or equivalent properties.

| Property | Description |
|---|---|
| `it.water.connectors.jobscheduler.quartz.*` | Quartz properties (see Quartz docs for full list) |
| ZooKeeper connection settings | Inherited from `ZookeeperConnector` if present |

## Dependencies
- `it.water.core:Core-api` — `BaseApi`, `BaseSystemApi`, `@FrameworkComponent`, `@OnActivate/@OnDeactivate`
- `it.water.service.rest:Rest-api` — `RestApi`, `@LoggedIn`
- `org.quartz-scheduler:quartz:2.3.2` — Quartz job scheduler
- `org.apache.curator:curator-framework:5.8.0` — ZooKeeper client for leader election
- `org.apache.curator:curator-recipes:5.8.0` — `LeaderLatch` recipe
- `it.water.connectors.zookeeper:ZookeeperConnector-api` — Optional: ZooKeeper connection

## Testing
- Unit tests: use an in-memory Quartz scheduler (set `org.quartz.jobStore.class=RAMJobStore`)
- Mock `WaterJob` with a simple cron expression (e.g., `"0/5 * * * * ?"`)
- Test: `addJob()` → `deleteJob()` → verify scheduler has no more triggers
- For cluster tests: use `TestingServer` (Curator test server) to simulate ZooKeeper
- If concrete REST methods are introduced later, test them with Karate only and never with direct JUnit calls to `JobSchedulerRestController`

## Code Generation Rules
- Implementing `WaterJob`: always validate `getCronExpression()` with `CronExpression.isValidExpression()` before calling `addJob()`
- `getJobParams()` values must be serializable — Quartz stores them in a `JobDataMap`
- `isActive() == false` means the job is defined but not scheduled — useful for disabling jobs without deleting them
- `JobKey` group defaults to `Scheduler.DEFAULT_GROUP` if not specified — use a meaningful group name for multi-tenant deployments
- NEVER call `scheduler.start()` directly — leadership listener controls it
- Quartz JDBC store requires DB schema: call `createQuartzTableIfNotExists()` during module initialization
- The current REST controller is only a placeholder; if it becomes concrete, test it exclusively via Karate
